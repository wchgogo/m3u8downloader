package com.wchgogo.m3u8downloader.worker;

import com.wchgogo.m3u8downloader.po.Task;
import com.wchgogo.m3u8downloader.service.ITaskService;
import com.wchgogo.m3u8downloader.util.Const;
import com.wchgogo.m3u8downloader.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Author: Wang Chao
 * Date: 2020/5/1 12:12
 * Description: 解析m3u8url
 */
@Slf4j
@Order(500)
@Service
public class UrlParser implements IWorker {
    @Resource
    private ITaskService taskService;

    @Override
    public boolean work(Task task) {
        String url = task.getUrl();
        BrowserMobProxy proxy = null;
        WebDriver driver = null;
        for (int i = 0; i < task.getRetryTime(); i++) {
            try {
                String data = HttpClientUtil.get(url);
                if (data.startsWith("#EXTM3U")) {
                    task.setM3u8url(url);
                    taskService.parseUrlSuccess(task);
                    log.info("[parseUrl] taskId={} retry={} success", task.getTaskId(), i);
                    return true;
                }

                System.setProperty("webdriver.chrome.driver", Const.WEBDRIVER_CHROME_DRIVER_PATH);
                proxy = new BrowserMobProxyServer();
                proxy.setTrustAllServers(true);
                proxy.start();
                proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT, CaptureType.RESPONSE_BINARY_CONTENT);
                proxy.newHar();
                Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
                seleniumProxy.setHttpProxy("localhost:" + proxy.getPort());
                seleniumProxy.setSslProxy("localhost:" + proxy.getPort());
                DesiredCapabilities seleniumCapabilities = DesiredCapabilities.chrome();
                seleniumCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, seleniumProxy);
                seleniumCapabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
                seleniumCapabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                ChromeOptions options = new ChromeOptions();
                options.merge(seleniumCapabilities);
                options.setHeadless(true);
                driver = new ChromeDriver(options);
                driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
                try {
                    driver.get(url);
                } catch (Exception e) {
                    log.error("[parseUrl] taskId={} retry={} timeout", task.getTaskId(), i, e);
                }

                String m3u8Url = null;
                for (HarEntry entry : proxy.getHar().getLog().getEntries()) {
                    if ("application/vnd.apple.mpegurl".equals(entry.getResponse().getContent().getMimeType())) {
                        String content = new String(Base64.getDecoder().decode(entry.getResponse().getContent().getText()));
                        if (StringUtils.isNotEmpty(content) && content.split("\n").length > 50) {
                            m3u8Url = entry.getRequest().getUrl();
                            break;
                        }
                    }
                }
                if (m3u8Url != null) {
                    task.setM3u8url(m3u8Url);
                    taskService.parseUrlSuccess(task);
                    log.info("[parseUrl] taskId={} retry={} success", task.getTaskId(), i);
                    return true;
                } else {
                    taskService.parseSeqFail(task);
                    log.info("[parseUrl] taskId={} fail", task.getTaskId());
                }
            } catch (Exception e) {
                log.error("[parseUrl] taskId={} retry={} fail", task.getTaskId(), i, e);
            } finally {
                if (driver != null) {
                    driver.quit();
                }
                if (proxy != null) {
                    proxy.abort();
                }
            }
        }
        taskService.parseSeqFail(task);
        log.info("[parseUrl] taskId={} fail", task.getTaskId());
        return false;
    }
}
