package com.hdu.handler;

import com.hdu.config.ExperimentProperties;
import com.hdu.utils.experiment.ExperimentStatus;
import com.hdu.utils.experiment.ExperimentStatusManager;
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

@ControllerAdvice
@RestController
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    ExperimentStatusWebsocketClient experimentStatusWebsocketClient;
    @Autowired
    ExperimentStatusManager experimentStatusManager;

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex) {
        logger.error("Unhandled exception caught: ", ex);
        experimentStatusWebsocketClient.send(ExperimentProperties.experimentId+" "+"TERMINATED");
        experimentStatusManager.setExperimentStatus(ExperimentProperties.experimentId, ExperimentStatus.TERMINATED);
        // Customize the response entity as needed
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}