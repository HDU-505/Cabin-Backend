package com.hdu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EOG implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String experiment_id;

    private String timeStamp;


    private String cnt;

    private String fpogx;

    private String fpogy;

    private String fpogs;

    private String fpogd;

    private String fpogid;

    private String fpogv;

    private String lpogx;

    private String lpogy;

    private String lpogv;

    private String rpogx;

    private String rpogy;

    private String rpogv;

    private String num;
}
