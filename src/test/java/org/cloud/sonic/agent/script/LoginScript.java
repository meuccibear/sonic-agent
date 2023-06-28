package org.cloud.sonic.agent.script;



import org.cloud.sonic.agent.tests.LogUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.cloud.sonic.agent.tests.handlers.AndroidStepHandler;
import com.android.ddmlib.IDevice;
import lombok.Getter;
import lombok.Setter;
import org.cloud.sonic.agent.bridge.android.AndroidDeviceBridgeTool;
import org.cloud.sonic.agent.common.interfaces.StepType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Ignore
public class LoginScript {
    AndroidStepHandler androidStepHandler;

    @Getter
    @Setter
    String udId = "829ed1f"; // your device udid

    @Before
    public void beforeClass() throws Exception {
        androidStepHandler = new AndroidStepHandler();

        IDevice iDevice = AndroidDeviceBridgeTool.getIDeviceByUdId(udId);
        androidStepHandler.setTestMode(0, 0, iDevice.getSerialNumber(), "no send server", "ddddddd");
        int port = AndroidDeviceBridgeTool.startUiaServer(iDevice);
        androidStepHandler.startAndroidDriver(iDevice, port);
    }

    @Test
    public void run(){
        LogUtil log = androidStepHandler.log;
        log.sendStepLog(StepType.INFO,"Hello","world");

    }

}
