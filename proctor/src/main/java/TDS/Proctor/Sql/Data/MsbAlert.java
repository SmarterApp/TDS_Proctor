package TDS.Proctor.Sql.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by jtreuting on 8/11/16.
 */
public class MsbAlert {
    private String sessionId;
    private String studentId;
    private String state;

    public MsbAlert(String sessionId, String studentId, String state) {

        this.sessionId = sessionId;
        this.studentId = studentId;
        this.state = state;
    }

    @JsonProperty("sessionId")
    public String getSessionId() {
        return sessionId;
    }

    @JsonProperty("studentId")
    public String getStudentId() {
        return studentId;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }
}
