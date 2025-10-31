package com.hdu.utils.websocket;


import com.hdu.experiment.ExperimentState;
import com.hdu.utils.IdGenerator;
import lombok.Data;

@Data
public class ExperimentStateMessage {
    private String id;
    private String machineId;
    private String experimentId;
    private ExperimentState state;

    // 附加字段
    private String experimentInfo;


    public ExperimentStateMessage(String machineId,String experimentId, ExperimentState state){
        // 随机生成id
        id = IdGenerator.generateStateMessageId();
        this.machineId = machineId;
        this.experimentId = experimentId;
        this.state = state;
    }
}
