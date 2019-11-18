package functions;

import com.microsoft.azure.functions.annotation.*;

import com.microsoft.azure.functions.*;
import utils.Scores;

public class UpdateRallFrontpageFunction {

    @FunctionName("update_rall_frontpage")
    public void updateRallFrontpage(@TimerTrigger(name = "keepAliveTrigger", schedule = "* */30 * * * *") String timerInfo,
                                ExecutionContext context) {
        Scores.calcAndPutRallFrontpageInCache();
    }
}
