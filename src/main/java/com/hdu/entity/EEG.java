package com.hdu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tbl_eeg")
public class EEG implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String experiment_id;

    private String timeStamp;

    private Double gnd;

    private Double ref;

    private Double afz;

    private Double af3;

    private Double af4;

    private Double af7;

    private Double af8;

    private Double pz;

    private Double p3;

    private Double p4;

}
