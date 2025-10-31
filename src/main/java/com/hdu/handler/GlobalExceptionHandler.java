package com.hdu.handler;

import com.hdu.exception.ErrorCode;
import com.hdu.exception.ExperimentException;
import com.hdu.experiment.ExperimentEvent;
import com.hdu.experiment.ExperimentStateMachine;
import com.hdu.utils.websocket.ExperimentStatusWebsocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    ExperimentStatusWebsocketClient experimentStatusWebsocketClient;

    private final ExperimentStateMachine experimentStateMachine = ExperimentStateMachine.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExperimentException.class)
    public final void handleExperimentExceptions(ExperimentException e) {
        logger.error("实验过程发生异常,异常代码为：" + e.getCode() + " 异常信息为：" + e.getMessage());
        if (Objects.equals(e.getCode(), ErrorCode.EXPERIMENT_ERROR.getCode())) {
            experimentStateMachine.handleEvent(ExperimentEvent.ERROR_OCCURED);
        }
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex) {
        logger.error("Unhandled exception caught: ", ex);
        experimentStateMachine.handleEvent(ExperimentEvent.ERROR_OCCURED);
        // Customize the response entity as needed
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}