/*
 *   sonic-agent  Agent of ZPUTech Cloud Real Machine Platform.
 *   Copyright (C) 2022 ZPUTechCloudOrg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.cloud.sonic.agent.websockets;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.ddmlib.IDevice;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.cloud.sonic.agent.bridge.android.AndroidDeviceBridgeTool;
import org.cloud.sonic.agent.common.config.WsEndpointConfigure;
import org.cloud.sonic.agent.common.maps.AndroidAPKMap;
import org.cloud.sonic.agent.common.maps.AndroidDeviceManagerMap;
import org.cloud.sonic.agent.common.maps.ScreenMap;
import org.cloud.sonic.agent.common.maps.WebSocketSessionMap;
import org.cloud.sonic.agent.tests.android.minicap.MiniCapUtil;
import org.cloud.sonic.agent.tests.android.scrcpy.ScrcpyServerUtil;
import org.cloud.sonic.agent.tests.handlers.AndroidMonitorHandler;
import org.cloud.sonic.agent.tools.BytesTool;
import org.cloud.sonic.agent.tools.ScheduleTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@ServerEndpoint(value = "/websockets/android/screen/{key}/{udId}/{token}", configurator = WsEndpointConfigure.class)
public class AndroidScreenWSServer implements IAndroidWSServer {
    @Value("${sonic.agent.key}")
    private String key;
    private Map<String, String> typeMap = new ConcurrentHashMap<>();
    private Map<String, String> picMap = new ConcurrentHashMap<>();

    private AndroidMonitorHandler androidMonitorHandler = new AndroidMonitorHandler();

    @OnOpen
    public void onOpen(Session session, @PathParam("key") String secretKey,
                       @PathParam("udId") String udId, @PathParam("token") String token) throws Exception {
        if (secretKey.length() == 0 || (!secretKey.equals(key)) || token.length() == 0) {
            log.info("Auth Failed!");
            return;
        }
        IDevice iDevice = AndroidDeviceBridgeTool.getIDeviceByUdId(udId);
        if (iDevice == null) {
            log.info("Target device is not connecting, please check the connection.");
            return;
        }

        // 禁止旋转屏幕 【耗时 4s】
        if (false) AndroidDeviceBridgeTool.screen(iDevice, "abort");

        session.getUserProperties().put("udId", udId);
        session.getUserProperties().put("id", String.format("%s-%s", this.getClass().getSimpleName(), udId));
        WebSocketSessionMap.addSession(session);
        saveUdIdMapAndSet(session, iDevice);

        if (false) {
            // 等待 控制手机websocket 启动
            int wait = 0;
            boolean isInstall = true;
            while (AndroidAPKMap.getMap().get(udId) == null || (!AndroidAPKMap.getMap().get(udId))) {
                Thread.sleep(500);
                wait++;
                log.info("wait:" + wait);
                if (wait >= 40) {
                    isInstall = false;
                    break;
                }
            }
            if (!isInstall) {
                log.info("Waiting for apk install timeout!");
                exit(session);
            }
        }

        // 使用设备 倒计时 定时器【耗时 0.5s】
        session.getUserProperties().put("schedule", ScheduleTool.schedule(() -> {
            log.info("time up!");
            if (session.isOpen()) {
                JSONObject errMsg = new JSONObject();
                errMsg.put("msg", "error");
                BytesTool.sendText(session, errMsg.toJSONString());
                exit(session);
            }
        }, BytesTool.remoteTimeout));

    }

    @OnClose
    public void onClose(Session session) {
        exit(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error(error.getMessage());
        error.printStackTrace();
        JSONObject errMsg = new JSONObject();
        errMsg.put("msg", "error");
        BytesTool.sendText(session, errMsg.toJSONString());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject msg = JSON.parseObject(message);
        log.info("{} send: {}", session.getUserProperties().get("id").toString(), msg);
        String udId = session.getUserProperties().get("udId").toString();
        // scrcpy minicap
        String detail = msg.getString("detail");
        switch (msg.getString("type")) {
            case "rotation" -> {
                IDevice iDevice = udIdMap.get(session);
                if (!androidMonitorHandler.isMonitorRunning(iDevice)) {
                    androidMonitorHandler.startMonitor(iDevice, res -> {
                        JSONObject rotationJson = new JSONObject();
                        rotationJson.put("msg", "rotation");
                        rotationJson.put("value", Integer.parseInt(res) * 90);
                        BytesTool.sendText(session, rotationJson.toJSONString());
                    });
                }
            }
            case "switch" -> {
                typeMap.put(udId, detail);
//                IDevice iDevice = udIdMap.get(session);
//                if (!androidMonitorHandler.isMonitorRunning(iDevice)) {
//                    log.info("【switch】【Start Monitor】time:{}", DateUtil.formatByDateTimeFormatter(new Date()));
//                    androidMonitorHandler.startMonitor(iDevice, res -> {
//                        JSONObject rotationJson = new JSONObject();
//                        rotationJson.put("msg", "rotation");
//                        rotationJson.put("value", Integer.parseInt(res) * 90);
//                        BytesTool.sendText(session, rotationJson.toJSONString());
//                        log.info("【startScreen】【Start】time:{}", DateUtil.formatByDateTimeFormatter(new Date()));
//                        startScreen(session);
//                        log.info("【startScreen】【End】time:{}", DateUtil.formatByDateTimeFormatter(new Date()));
//                    });
//                    log.info("【switch】【End】");
//                } else {
//                    startScreen(session);
//                }
                startScreen(session);
            }
            case "pic" -> {
                picMap.put(udId, detail);
                startScreen(session);
            }
        }
    }

    private void startScreen(Session session) {
        log.info("【AndroidScreenWSServer】【startScreen】【Start】");
        IDevice iDevice = udIdMap.get(session);
        if (iDevice != null) {
            Thread old = ScreenMap.getMap().get(session);
            if (old != null) {
                old.interrupt();
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (ScreenMap.getMap().get(session) != null);
            }
            typeMap.putIfAbsent(iDevice.getSerialNumber(), "scrcpy");
            switch (typeMap.get(iDevice.getSerialNumber())) {
                case "scrcpy" -> {
                    ScrcpyServerUtil scrcpyServerUtil = new ScrcpyServerUtil();
                    Thread scrcpyThread = scrcpyServerUtil.start(iDevice.getSerialNumber(), AndroidDeviceManagerMap.getRotationMap().getOrDefault(iDevice.getSerialNumber(), 0), session);
                    ScreenMap.getMap().put(session, scrcpyThread);
                }
                case "minicap" -> {
                    MiniCapUtil miniCapUtil = new MiniCapUtil();
                    AtomicReference<String[]> banner = new AtomicReference<>(new String[24]);
                    Thread miniCapThread = miniCapUtil.start(
                            iDevice.getSerialNumber(), banner, null,
                            picMap.get(iDevice.getSerialNumber()) == null ? "middle" : picMap.get(iDevice.getSerialNumber()),
                            AndroidDeviceManagerMap.getRotationMap().getOrDefault(iDevice.getSerialNumber(), 0), session
                    );
                    ScreenMap.getMap().put(session, miniCapThread);
                }
            }
            JSONObject picFinish = new JSONObject();
            picFinish.put("msg", "picFinish");
            BytesTool.sendText(session, picFinish.toJSONString());
        }
        log.info("【AndroidScreenWSServer】【startScreen】【End】");
    }

    private void exit(Session session) {
        synchronized (session) {
            ScheduledFuture<?> future = (ScheduledFuture<?>) session.getUserProperties().get("schedule");
            if (null != future)
                future.cancel(true);
            String udId = session.getUserProperties().get("udId").toString();
            androidMonitorHandler.stopMonitor(udIdMap.get(session));
            WebSocketSessionMap.removeSession(session);
            removeUdIdMapAndSet(session);
            AndroidDeviceManagerMap.getRotationMap().remove(udId);
            if (ScreenMap.getMap().get(session) != null) {
                ScreenMap.getMap().get(session).interrupt();
            }
            typeMap.remove(udId);
            picMap.remove(udId);
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("{} : quit.", session.getUserProperties().get("id").toString());
        }
    }
}
