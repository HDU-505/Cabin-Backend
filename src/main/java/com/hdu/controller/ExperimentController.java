package com.hdu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hdu.entity.Experiment;
import com.hdu.entity.SearchForm;
import com.hdu.service.IExperimentService;
import com.hdu.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author DZL
 * @since 2024-05-28
 */
@RestController
@RequestMapping("/experiment")
@CrossOrigin
public class ExperimentController {

    @Autowired
    private IExperimentService experimentService;

    /*
     *   获得所有实验信息，包括被试信息
     *
     * */
    @PostMapping("/getAllExperiment/{current}/{limit}")
    public R getAllExperiments(@PathVariable long current,
                               @PathVariable long limit,
                               @RequestBody SearchForm searchForm) {
        //创建Page对象
        Page<Experiment> page = new Page<>(current, limit);

        //获取所有数据并返回
        experimentService.getAllExperiments(page, searchForm);
        long total = page.getTotal();
        List<Experiment> records = page.getRecords();
        return R.ok().data("total", total).data("rows", records);
    }

    /*
    *   创建实验
    * */
    @PostMapping("/createExperiment")
    public R createExperiment(@RequestBody Experiment experiment) {
        //TODO 开始实验的标志，需要根据该标志进行数据缓存
        //创建实验处理过程
        String experiment_id = experimentService.createExperiment(experiment);
        return experiment_id != null ? R.ok().data("id", experiment_id) : R.error();
    }

    /*
    *   开始实验
    * */
    @GetMapping("/startExperiment/{experiment_id}")
    public R startExperiment(@PathVariable String experiment_id) {
        //开始实验处理过程
        boolean flag = experimentService.startExperiment(experiment_id);
        return flag ? R.ok() : R.error();
    }

    /*
    *   结束实验
    * */
    @GetMapping("/endExperiment/{experiment_id}")
    public R endExperiment(@PathVariable String experiment_id) {
        //结束实验处理过程
        boolean flag = experimentService.endExperiment(experiment_id);
        return flag ? R.ok() : R.error();
    }
    /*
    *   增加历史数据，主要是上传数据（但暂不实现）
    * */
    //TODO 暂不实现
    @PostMapping("/addExperiment")
    public R addExperiment(@RequestBody Experiment experiment) {
        return R.ok();
    }

    /*
    *   根据id,查询实验信息
    * */
    @GetMapping("/getExperimentById/{id}")
    public R getExperimentById(@PathVariable String id) {
        //获取实验信息
        Experiment experiment = experimentService.getById(id);
        return R.ok().data("data", experiment);
    }

    /*
    *   根据id，批量删除实验信息
    * */
    @PostMapping("/delExperimentByIds")
    public R delExperimentByIds(@RequestBody List<String> ids) {
        //删除实验信息
        boolean flag = experimentService.delExperimentByIds(ids);

        return flag ? R.ok() : R.error();
    }

    /**
     * 下载指定实验数据
     *
     * @param id
     * @return
     */
    @GetMapping("/downloadExperimentById/{id}")
    public ResponseEntity<Resource> exportData(@PathVariable String id) {
        byte[] zipBytes = experimentService.getDataZip(id);
        // 打印ZIP文件字节数组长度和部分内容
        System.out.println("ZIP file length: " + zipBytes.length);
        System.out.println("ZIP file content (first 100 bytes): " + Arrays.toString(Arrays.copyOfRange(zipBytes, 0, 100)));

        ByteArrayResource resource = new ByteArrayResource(zipBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename("data.zip").build());

        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(resource);
    }

}
