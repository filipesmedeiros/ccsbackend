package functions;

import com.microsoft.azure.functions.annotation.*;

import com.microsoft.azure.functions.*;

public class UpdateRallFrontpageFunction {

    @FunctionName("update_rall_frontpage")
    public void updateRallFrontpage(@TimerTrigger(name = "keepAliveTrigger", schedule = "*/20 * * * * *") String timerInfo,
                                ExecutionContext context) {
        // TODO
    }
}
