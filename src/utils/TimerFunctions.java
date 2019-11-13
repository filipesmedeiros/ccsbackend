package utils;

import com.microsoft.azure.functions.annotation.*;

import com.microsoft.azure.functions.*;

public class TimerFunctions {

    @FunctionName("update_rall_frontpage")
    public void updateRalFrontpage(@TimerTrigger(name = "keepAliveTrigger", schedule = "*/20 * * * * *") String timerInfo,
                                ExecutionContext context) {
        // TODO
    }
}
