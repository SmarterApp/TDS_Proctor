package TDS.Proctor.Web.interceptors;

import org.opentestsystem.delivery.logging.EventLogger;
import org.opentestsystem.delivery.logging.ProctorEventLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class ProctorLoggingInterceptor extends HandlerInterceptorAdapter {
    private EventLogger logger;

    @Autowired
    public ProctorLoggingInterceptor(EventLogger logger) {
        this.logger = logger;
    }


    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        logger.info(ProctorEventLogger.APP, request.getPathInfo(),
            EventLogger.Checkpoint.ENTER.name(), request.getSession().getId(), "StudentLoggingInterceptor", null);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {


        Map<EventLogger.IEventData, Object> fields = new HashMap<>();
        fields.put(ProctorEventLogger.ProctorEventData.EXAM, "exam-sample");

        logger.info(ProctorEventLogger.APP, request.getPathInfo(),
            EventLogger.Checkpoint.EXIT.name(), request.getSession().getId(), "StudentLoggingInterceptor", fields);
    }

}