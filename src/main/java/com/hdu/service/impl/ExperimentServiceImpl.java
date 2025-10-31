package com.hdu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hdu.config.ExperimentProperties;
import com.hdu.config.RedisKeyConfig;
import com.hdu.entity.*;
import com.hdu.experiment.ExperimentEvent;
import com.hdu.experiment.ExperimentState;
import com.hdu.experiment.ExperimentStateMachine;
import com.hdu.mapper.ExperimentMapper;
import com.hdu.service.IExperimentService;
import com.hdu.utils.CsvUtils;
import com.hdu.utils.IdGenerator;
import com.hdu.utils.ZipUtils;
import com.hdu.utils.cnosdb.CnosdbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author DZL
 * @since 2024-05-28
 */
@Service
public class ExperimentServiceImpl extends ServiceImpl<ExperimentMapper, Experiment> implements IExperimentService {
    @Autowired
    ExperimentMapper experimentMapper;

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Autowired
    CnosdbUtil cnosdbUtil;

    private final ExperimentStateMachine experimentStateMachine = ExperimentStateMachine.getInstance();

    /*
    *   获得所有的实验信息
    *
    * */
    @Transactional
    @Override
    public void getAllExperiments(Page<Experiment> page, SearchForm searchForm) {
        // 获取所有的实验数据
        QueryWrapper<Experiment> queryWrapper = new QueryWrapper<>();
        if (searchForm != null){
            if (searchForm.getId() != null && !searchForm.getId().isEmpty()){
                queryWrapper.eq("id", searchForm.getId());
            }
            if (searchForm.getName() != null && !searchForm.getName().trim().isEmpty()){
                queryWrapper.like("name", searchForm.getName());
            }
            if (searchForm.getGender() != null && !searchForm.getGender().trim().isEmpty()){
                queryWrapper.eq("gender", searchForm.getGender());
            }
            if (searchForm.getAge() != null && searchForm.getAge() != 0){
                queryWrapper.eq("age", searchForm.getAge());
            }
            if (searchForm.getDate() != null && !searchForm.getDate().isEmpty()){
                queryWrapper.ge("start_time", searchForm.getDate().get(0));
                queryWrapper.le("end_time", searchForm.getDate().get(1));
            }
        }
        //按条件和分页进行查找
        experimentMapper.selectPage(page,queryWrapper);
    }

    /*
    *   创建实验的处理函数
    *
    * */

    @Override
    public String createExperiment(Experiment experiment) {
        //判断是否当前有实验在进行中
        String on_off = (String) redisTemplate.opsForValue().get(RedisKeyConfig.EXPERIMENT_ON_OFF_STATUS);
        if (on_off != null && on_off.equals("on")){
            return null;
        }
        //首先判断信息是否完整
        if (experiment == null){return null;}
        if (experiment.getName() == null || experiment.getAge() == null || experiment.getGender() == null || experiment.getParadigmId() == null){
            return null;
        }
        //把时间信息完善
        //生成唯一性id
        experiment.setId(IdGenerator.generate20CharId());

        //将实验id存储到全局变量中
        ExperimentProperties.experimentId = experiment.getId();

        //把信息存入缓存中
        redisTemplate.opsForValue().set(RedisKeyConfig.EXPERIMENT_INFO_KEY_PREFIX +experiment.getId(),experiment);

        //修改实验状态为“等待”
        experimentStateMachine.handleEvent(ExperimentEvent.START_PREPARATION);

        //把是否在实验中的状态设定为on
        redisTemplate.opsForValue().set(RedisKeyConfig.EXPERIMENT_ON_OFF_STATUS,"on");


        return experiment.getId();
    }

    /*
    *   开始实验的处理函数
    *
    * */

    @Override
    public boolean startExperiment(String experimentId) {
        //判断id是否为空
        if (experimentId == null || experimentId.trim().isEmpty()){
            return false;
        }
        //判断当前的实验状态是否为“等待”,只能单向转换
        ExperimentState currState = experimentStateMachine.getCurrentState();
        if (currState != ExperimentState.PREPARING){return false;}

        //从redis中获取对象
        Experiment experiment = getExperimentByRedis(experimentId);

        //判断是否为空
        if (experiment == null) return false;

        //更新实验开始时间
        // 获取当前时间
        Date currentTime = new Date(System.currentTimeMillis());
        experiment.setStartTime(currentTime);

        //把新的experiment对象更新到redis
        redisTemplate.opsForValue().set(RedisKeyConfig.EXPERIMENT_INFO_KEY_PREFIX+experimentId,experiment);

        //更新实验状态为“开始”
        experimentStateMachine.handleEvent(ExperimentEvent.START_EXPERIMENT);

        return true;
    }

    /*
    *   结束实验处理函数
    *
    * */

    @Override
    public boolean endExperiment(String experimentId) {
        //判断是否为空
         if (experimentId == null || experimentId.trim().isEmpty()) return false;

        // 判断当前实验是否处于开始状态
        ExperimentState currState = experimentStateMachine.getCurrentState();
        if (currState != ExperimentState.RUNNING){return false;}

        //从redis中获取实验信息
        Experiment experiment = getExperimentByRedis(experimentId);

        //判断是否为空
        if (experiment == null) return false;

        //更新结束时间
        Date currentTime = new Date(System.currentTimeMillis());
        experiment.setEndTime(currentTime);

        //更新实验状态为“结束”
        experimentStateMachine.handleEvent(ExperimentEvent.END_EXPERIMENT);

        //将实验信息存储到数据库
        experimentMapper.insert(experiment);

        //把redis中的实验对象删除，同时删除状态管理
        redisTemplate.delete(RedisKeyConfig.EXPERIMENT_STATUS_KEY_PREFIX+experimentId);
        redisTemplate.delete(RedisKeyConfig.EXPERIMENT_INFO_KEY_PREFIX+experimentId);
//        redisTemplate.opsForValue().set(RedisKeyConfig.EXPERIMENT_ON_OFF_STATUS,"off");
        while (true){
            if ("off".equals(redisTemplate.opsForValue().get(RedisKeyConfig.EXPERIMENT_ON_OFF_STATUS))){
                //清空所有的缓存数据
                redisTemplate.delete(RedisKeyConfig.EXPERIMENT_DATA_EEG_KEY);
                redisTemplate.delete(RedisKeyConfig.EXPERIMENT_DATA_EOG_KEY);
                redisTemplate.delete(RedisKeyConfig.EXPERIMENT_DATA_GSR_KEY);
                redisTemplate.delete(RedisKeyConfig.EXPERIMENT_DATA_HR_KEY);
                redisTemplate.delete(RedisKeyConfig.EXPERIMENT_DATA_LABEL_KEY);
                break;
            }
        }

        return true;
    }

    /*
    *   获取指定id下的所有数据,各自生成csv文件并打包成压缩包
    *
    * */
    @Override
    public byte[] getDataZip(String id) {
        // 构建SQL语句
        String eeg_select_sql = "SELECT * FROM eeg WHERE experiment_id = '" + id + "';";
        String eog_select_sql = "SELECT * FROM eog WHERE experiment_id = '" + id + "';";
        String hr_select_sql = "SELECT * FROM hr WHERE experiment_id = '" + id + "';";
        String gsr_select_sql = "SELECT * FROM gsr WHERE experiment_id = '" + id + "';";
        String label_select_sql = "SELECT * FROM label WHERE experiment_id = '" + id + "';";

        List<EEG> eegList = new ArrayList<>();
        //查询数据
        ResultSet eegResultSet = cnosdbUtil.executeQuery(eeg_select_sql);
        //解析数据
        try{
            while (eegResultSet.next()) {
                EEG eegData = new EEG();
                eegData.setId(eegResultSet.getString("id"));
                eegData.setExperiment_id(eegResultSet.getString("experiment_id"));
                eegData.setGnd(eegResultSet.getDouble("gnd"));
                eegData.setRef(eegResultSet.getDouble("ref"));
                eegData.setAfz(eegResultSet.getDouble("afz"));
                eegData.setAf3(eegResultSet.getDouble("af3"));
                eegData.setAf4(eegResultSet.getDouble("af4"));
                eegData.setAf7(eegResultSet.getDouble("af7"));
                eegData.setAf8(eegResultSet.getDouble("af8"));
                eegData.setPz(eegResultSet.getDouble("pz"));
                eegData.setP3(eegResultSet.getDouble("p3"));
                eegData.setP4(eegResultSet.getDouble("p4"));
                eegData.setTimeStamp(eegResultSet.getTimestamp("time").getTime()+"");
                eegList.add(eegData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        //eog数据
        List<EOG> eogDataList = new ArrayList<>();

        try (ResultSet resultSet = cnosdbUtil.executeQuery(eog_select_sql)) {
            while (resultSet.next()) {
                EOG eogData = new EOG();
                eogData.setId(resultSet.getString("id"));
                eogData.setExperiment_id(resultSet.getString("experiment_id"));
                eogData.setCnt(resultSet.getString("cnt"));
                eogData.setFpogx(resultSet.getString("fpogx"));
                eogData.setFpogy(resultSet.getString("fpogy"));
                eogData.setFpogs(resultSet.getString("fpogs"));
                eogData.setFpogd(resultSet.getString("fpogd"));
                eogData.setFpogid(resultSet.getString("fpogid"));
                eogData.setFpogv(resultSet.getString("fpogv"));
                eogData.setLpogx(resultSet.getString("lpogx"));
                eogData.setLpogy(resultSet.getString("lpogy"));
                eogData.setLpogv(resultSet.getString("lpogv"));
                eogData.setRpogx(resultSet.getString("rpogx"));
                eogData.setRpogy(resultSet.getString("rpogy"));
                eogData.setRpogv(resultSet.getString("rpogv"));
                eogData.setTimeStamp(eegResultSet.getTimestamp("time").getTime()+"");

                eogDataList.add(eogData);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //hr数据
        List<HR> hrList = new ArrayList<>();

        try (ResultSet resultSet = cnosdbUtil.executeQuery(hr_select_sql)) {
            while (resultSet.next()) {
                HR hrData = new HR();
                hrData.setId(resultSet.getString("id"));
                hrData.setExperiment_id(resultSet.getString("experiment_id"));
                hrData.setData(resultSet.getDouble("data"));
                hrData.setTimeStamp(eegResultSet.getTimestamp("time").getTime()+"");

                hrList.add(hrData);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //gsr数据
        List<GSR> gsrList = new ArrayList<>();

        try (ResultSet resultSet = cnosdbUtil.executeQuery(gsr_select_sql)) {
            while (resultSet.next()) {
                GSR gsrData = new GSR();
                gsrData.setId(resultSet.getString("id"));
                gsrData.setExperiment_id(resultSet.getString("experiment_id"));
                gsrData.setData(resultSet.getDouble("data"));
                gsrData.setTimeStamp(eegResultSet.getTimestamp("time").getTime()+"");

                gsrList.add(gsrData);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //label数据
        List<Label> labelList = new ArrayList<>();

        try (ResultSet resultSet = cnosdbUtil.executeQuery(label_select_sql)) {
            while (resultSet.next()) {
                Label labelData = new Label();
                labelData.setId(resultSet.getString("id"));
                labelData.setExperiment_id(resultSet.getString("experiment_id"));
                labelData.setLabel(resultSet.getString("label"));
                labelData.setTimeStamp(eegResultSet.getTimestamp("time").getTime()+"");

                labelList.add(labelData);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //转化为csv文件
//        byte[] eeg_csv = CsvUtils.generateCsvBytes(eegList,EEG.class,new String[] { "id", "experiment_id", "gnd" ,"ref","afz","af3","af4","af7","af8","pz","p3","p4","timeStamp"});
//        byte[] eog_csv = CsvUtils.generateCsvBytes(eogDataList,EOG.class,new String[] { "id", "experiment_id", "cnt" ,"fpogx","fpogy","fpogs","fpogd","fpogid","fpogv","lpogx","lpogy","lpogv","rpogx","rpogy","rpogv","timeStamp"});
//        byte[] hr_csv = CsvUtils.generateCsvBytes(hrList,HR.class,new String[] { "id", "experiment_id", "data" ,"timeStamp"});
//        byte[] gsr_csv = CsvUtils.generateCsvBytes(gsrList,GSR.class,new String[] { "id", "experiment_id", "data" ,"timeStamp"});
//        byte[] label_csv = CsvUtils.generateCsvBytes(labelList,Label.class,new String[] { "id", "experiment_id", "label" ,"timeStamp"});

        // 转换成excel文件
        byte[] eeg_excel = CsvUtils.generateExcelBytes(eegList, EEG.class, new String[]{"id", "experiment_id", "gnd", "ref", "afz", "af3", "af4", "af7", "af8", "pz", "p3", "p4", "timeStamp"});
        byte[] eog_excel = CsvUtils.generateExcelBytes(eogDataList, EOG.class, new String[]{"id", "experiment_id", "cnt", "fpogx", "fpogy", "fpogs", "fpogd", "fpogid", "fpogv", "lpogx", "lpogy", "lpogv", "rpogx", "rpogy", "rpogv", "timeStamp"});
        byte[] hr_excel = CsvUtils.generateExcelBytes(hrList, HR.class, new String[]{"id", "experiment_id", "data", "timeStamp"});
        byte[] gsr_excel = CsvUtils.generateExcelBytes(gsrList, GSR.class, new String[]{"id", "experiment_id", "data", "timeStamp"});
        byte[] label_excel = CsvUtils.generateExcelBytes(labelList, Label.class, new String[]{"id", "experiment_id", "label", "timeStamp"});


        //打包成压缩包
        Map<String,byte[]> csvBytesMap = new HashMap<>();
//        csvBytesMap.put(id+"-"+"eeg.csv",eeg_csv);
//        csvBytesMap.put(id+"-"+"eog.csv",eog_csv);
//        csvBytesMap.put(id+"-"+"hr.csv",hr_csv);
//        csvBytesMap.put(id+"-"+"gsr.csv",gsr_csv);
//        csvBytesMap.put(id+"-"+"label.csv",label_csv);

        //同样把excel文件打入压缩包
        csvBytesMap.put(id+"-"+"eeg.xlsx",eeg_excel);
        csvBytesMap.put(id+"-"+"eog.xlsx",eog_excel);
        csvBytesMap.put(id+"-"+"hr.xlsx",hr_excel);
        csvBytesMap.put(id+"-"+"gsr.xlsx",gsr_excel);
        csvBytesMap.put(id+"-"+"label.xlsx",label_excel);


        byte[] zipBytes = ZipUtils.zipCsvBytes(csvBytesMap);
        System.out.println("ZIP file size: " + zipBytes.length + " bytes");
        return zipBytes;
    }

    /*
    *   按照ids删除实验信息
    * */

    @Override
    public boolean delExperimentByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()){
            return true;
        }
        //删除数据
        //TODO cnosdb以experiment_id作为删除条件不可以执行。

        try{
            for (String id : ids){
                Experiment experiment = experimentMapper.selectById(id);
                String del_sql_eeg = "delete from eeg where time >= "+experiment.getStartTime().getTime()+" and time <= "+experiment.getEndTime().getTime()+";";
                String del_sql_eog = "delete from eog where time >= "+experiment.getStartTime().getTime()+" and time <= "+experiment.getEndTime().getTime()+";";
                String del_sql_hr = "delete from hr where time >= "+experiment.getStartTime().getTime()+" and time <= "+experiment.getEndTime().getTime()+";";
                String del_sql_gsr = "delete from gsr where time >= "+experiment.getStartTime().getTime()+" and time <= "+experiment.getEndTime().getTime()+";";
                String del_sql_label = "delete from label where time >= "+experiment.getStartTime().getTime()+" and time <= "+experiment.getEndTime().getTime()+";";

                cnosdbUtil.executeDel(del_sql_eeg);
                cnosdbUtil.executeDel(del_sql_eog);
                cnosdbUtil.executeDel(del_sql_hr);
                cnosdbUtil.executeDel(del_sql_gsr);
                cnosdbUtil.executeDel(del_sql_label);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        experimentMapper.deleteBatchIds(ids);
        return true;
    }

    public Experiment getExperimentByRedis(String experiment_id){
        Object object = redisTemplate.opsForValue().get(RedisKeyConfig.EXPERIMENT_INFO_KEY_PREFIX+experiment_id);
        if (object instanceof Experiment){
            return (Experiment) object;
        }
        return null;
    }
}
