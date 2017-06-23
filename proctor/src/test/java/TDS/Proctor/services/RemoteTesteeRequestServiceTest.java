/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package TDS.Proctor.services;

import TDS.Proctor.Services.remote.RemoteTesteeRequestService;
import TDS.Proctor.Sql.Data.Abstractions.ExamPrintRequestRepository;
import TDS.Proctor.Sql.Data.Abstractions.ExamRepository;
import TDS.Proctor.Sql.Data.Abstractions.ITesteeRequestService;
import TDS.Proctor.Sql.Data.TesteeRequest;
import TDS.Proctor.Sql.Data.TesteeRequests;
import TDS.Proctor.performance.dao.ProctorUserDao;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamPrintRequest;
import tds.exam.ExamPrintRequestStatus;
import tds.exam.ExpandableExamPrintRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteTesteeRequestServiceTest {
    private RemoteTesteeRequestService service;

    @Mock
    private ITesteeRequestService legacyTestOpportunityService;

    @Mock
    private ExamPrintRequestRepository mockExamPrintRequestRepository;

    @Mock
    private ProctorUserDao proctorUserDao;

    @Before
    public void setup() {
        service = new RemoteTesteeRequestService(legacyTestOpportunityService, mockExamPrintRequestRepository,
            false, true, proctorUserDao);
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowForAccessDenied() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final long proctorId = 2112;

        when(proctorUserDao.validateProctorSession(proctorId, sessionId, browserId)).thenReturn("Invalid");
        service.getCurrentTesteeRequests(examId, sessionId, proctorId, browserId);
    }

    @Test
    public void shouldGetCurrentTesteeRequests() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final long proctorId = 2112;

        ExamPrintRequest request1 = new ExamPrintRequest.Builder(UUID.randomUUID())
            .withSessionId(sessionId)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withExamId(examId)
            .withParameters("params")
            .withType(ExamPrintRequest.REQUEST_TYPE_PRINT_ITEM)
            .withValue("/path/to/resource")
            .withCreatedAt(Instant.now())
            .withItemPosition(4)
            .withPagePosition(2)
            .withDescription("request 1")
            .build();
        ExamPrintRequest request2 = new ExamPrintRequest.Builder(UUID.randomUUID())
            .withSessionId(sessionId)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withExamId(examId)
            .withParameters("params")
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .withValue("/path/to/resource")
            .withCreatedAt(Instant.now())
            .withItemPosition(0)
            .withPagePosition(2)
            .withDescription("request 2")
            .build();

        when(proctorUserDao.validateProctorSession(proctorId, sessionId, browserId)).thenReturn(null);
        when(mockExamPrintRequestRepository.findUnfulfilledRequests(examId, sessionId)).thenReturn(Arrays.asList(request1, request2));

        TesteeRequests requests = service.getCurrentTesteeRequests(examId, sessionId, proctorId, browserId);
        verify(proctorUserDao).validateProctorSession(proctorId, sessionId, browserId);
        verify(mockExamPrintRequestRepository).findUnfulfilledRequests(examId, sessionId);

        assertThat(requests).hasSize(2);
        TesteeRequest testeeRequest1 = requests.get(0);
        assertThat(testeeRequest1.getKey()).isEqualTo(request1.getId());
        assertThat(testeeRequest1.getDateSubmitted()).isEqualTo(request1.getCreatedAt().toDate());
        assertThat(testeeRequest1.getItemPage()).isEqualTo(request1.getPagePosition());
        assertThat(testeeRequest1.getItemPosition()).isEqualTo(request1.getItemPosition());
        assertThat(testeeRequest1.getOppKey()).isEqualTo(request1.getExamId());
        assertThat(testeeRequest1.getSessionKey()).isEqualTo(request1.getSessionId());
        assertThat(testeeRequest1.getRequestType()).isEqualTo(request1.getType());
        assertThat(testeeRequest1.getRequestValue()).isEqualTo(request1.getValue());
        assertThat(testeeRequest1.getRequestDesc()).isEqualTo(request1.getDescription());
        assertThat(testeeRequest1.getDateFulfilled()).isNull();

        TesteeRequest testeeRequest2 = requests.get(1);
        assertThat(testeeRequest2.getKey()).isEqualTo(request2.getId());
        assertThat(testeeRequest2.getDateSubmitted()).isEqualTo(request2.getCreatedAt().toDate());
        assertThat(testeeRequest2.getItemPage()).isEqualTo(request2.getPagePosition());
        assertThat(testeeRequest2.getItemPosition()).isEqualTo(request2.getItemPosition());
        assertThat(testeeRequest2.getOppKey()).isEqualTo(request2.getExamId());
        assertThat(testeeRequest2.getSessionKey()).isEqualTo(request2.getSessionId());
        assertThat(testeeRequest2.getRequestType()).isEqualTo(request2.getType());
        assertThat(testeeRequest2.getRequestValue()).isEqualTo(request2.getValue());
        assertThat(testeeRequest2.getRequestDesc()).isEqualTo(request2.getDescription());
        assertThat(testeeRequest2.getDateFulfilled()).isNull();

    }

    @Test
    public void shouldGetApprovedTesteeRequests() throws Exception {
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final long proctorId = 2112;

        ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID())
            .withSessionId(sessionId)
            .withStatus(ExamPrintRequestStatus.APPROVED)
            .withChangedAt(Instant.now())
            .withExamId(UUID.randomUUID())
            .withParameters("params")
            .withType(ExamPrintRequest.REQUEST_TYPE_PRINT_ITEM)
            .withValue("/path/to/resource")
            .withCreatedAt(Instant.now())
            .withItemPosition(4)
            .withPagePosition(2)
            .withDescription("request 1")
            .build();

        when(proctorUserDao.validateProctorSession(proctorId, sessionId, browserId)).thenReturn(null);
        when(mockExamPrintRequestRepository.findApprovedRequests(sessionId)).thenReturn(Arrays.asList(request));

        TesteeRequests testeeRequests = service.getApprovedTesteeRequests(sessionId, proctorId, browserId);

        verify(proctorUserDao).validateProctorSession(proctorId, sessionId, browserId);
        verify(mockExamPrintRequestRepository).findApprovedRequests(sessionId);

        assertThat(testeeRequests).hasSize(1);
        TesteeRequest testeeRequest = testeeRequests.get(0);
        assertThat(testeeRequest.getKey()).isEqualTo(request.getId());
        assertThat(testeeRequest.getDateSubmitted()).isEqualTo(request.getCreatedAt().toDate());
        assertThat(testeeRequest.getItemPage()).isEqualTo(request.getPagePosition());
        assertThat(testeeRequest.getItemPosition()).isEqualTo(request.getItemPosition());
        assertThat(testeeRequest.getOppKey()).isEqualTo(request.getExamId());
        assertThat(testeeRequest.getSessionKey()).isEqualTo(request.getSessionId());
        assertThat(testeeRequest.getRequestType()).isEqualTo(request.getType());
        assertThat(testeeRequest.getRequestValue()).isEqualTo(request.getValue());
        assertThat(testeeRequest.getRequestDesc()).isEqualTo(request.getDescription());
        assertThat(testeeRequest.getDateFulfilled()).isEqualTo(request.getChangedAt().toDate());
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowForAccessDeniedFindApproved() throws Exception {
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final long proctorId = 2112;

        when(proctorUserDao.validateProctorSession(proctorId, sessionId, browserId)).thenReturn("Invalid");
        service.getApprovedTesteeRequests(sessionId, proctorId, browserId);
    }

    @Test
    public void shouldDenyTesteeRequest() throws Exception {
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final UUID requestId = UUID.randomUUID();
        final String reason = "Some reason";
        final long proctorId = 2112;

        when(proctorUserDao.validateProctorSession(proctorId, sessionId, browserId)).thenReturn(null);
        boolean isSuccessful = service.denyTesteeRequest(sessionId, proctorId, browserId, requestId, reason);
        assertThat(isSuccessful).isTrue();
        verify(mockExamPrintRequestRepository).denyPrintRequest(requestId, reason);
        verify(proctorUserDao).validateProctorSession(proctorId, sessionId, browserId);
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowForAccessDeniedDenyRequest() throws Exception {
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final long proctorId = 2112;
        final UUID requestId = UUID.randomUUID();
        final String reason = "Some reason";

        when(proctorUserDao.validateProctorSession(proctorId, sessionId, browserId)).thenReturn("Invalid");
        service.denyTesteeRequest(sessionId, proctorId, browserId, requestId, reason);
    }

    @Test
    public void shouldFindTesteeRequestValues() throws ReturnStatusException {
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final long proctorId = 2112;
        final UUID requestId = UUID.randomUUID();

        Exam exam = new Exam.Builder()
            .withId(UUID.randomUUID())
            .withLanguageCode("ENU")
            .withAttempts(3)
            .withStudentId(2112)
            .withStudentName("Peter Steele")
            .withAssessmentId("Assessment ID")
            .build();

        ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID())
            .withSessionId(sessionId)
            .withStatus(ExamPrintRequestStatus.APPROVED)
            .withChangedAt(Instant.now())
            .withExamId(exam.getId())
            .withParameters("params")
            .withType(ExamPrintRequest.REQUEST_TYPE_PRINT_ITEM)
            .withValue("/path/to/resource")
            .withCreatedAt(Instant.now())
            .withItemPosition(4)
            .withPagePosition(2)
            .withItemResponse("Response")
            .withDescription("request 1")
            .withParameters("request: params;")
            .build();

        ExpandableExamPrintRequest expandableRequest = new ExpandableExamPrintRequest.Builder(request)
            .withExam(exam)
            .build();

        when(proctorUserDao.validateProctorSession(proctorId, sessionId, browserId)).thenReturn(null);
        when(mockExamPrintRequestRepository.findRequestAndApprove(requestId)).thenReturn(expandableRequest);

        TesteeRequest testeeRequest = service.getTesteeRequestValues(sessionId, proctorId, browserId, requestId, true);

        verify(proctorUserDao).validateProctorSession(proctorId, sessionId, browserId);
        verify(mockExamPrintRequestRepository).findRequestAndApprove(requestId);

        assertThat(testeeRequest.getKey()).isEqualTo(request.getId());
        assertThat(testeeRequest.getDateSubmitted()).isEqualTo(request.getCreatedAt().toDate());
        assertThat(testeeRequest.getItemPage()).isEqualTo(request.getPagePosition());
        assertThat(testeeRequest.getItemPosition()).isEqualTo(request.getItemPosition());
        assertThat(testeeRequest.getOppKey()).isEqualTo(request.getExamId());
        assertThat(testeeRequest.getSessionKey()).isEqualTo(request.getSessionId());
        assertThat(testeeRequest.getRequestType()).isEqualTo(request.getType());
        assertThat(testeeRequest.getRequestValue()).isEqualTo(request.getValue());
        assertThat(testeeRequest.getRequestDesc()).isEqualTo(request.getDescription());
        assertThat(testeeRequest.getDateFulfilled()).isEqualTo(request.getChangedAt().toDate());
        assertThat(testeeRequest.getTestID()).isEqualTo(exam.getAssessmentId());
        assertThat(testeeRequest.getTesteeKey()).isEqualTo(exam.getStudentId());
        assertThat(testeeRequest.getTesteeName()).isEqualTo(exam.getStudentName());
        assertThat(testeeRequest.getLanguage()).isEqualTo(exam.getLanguageCode());
        assertThat(testeeRequest.getAccCode()).isEqualTo(exam.getLanguageCode());
        assertThat(testeeRequest.getRequestParameters()).isEqualTo(request.getParameters());
        assertThat(testeeRequest.getOpportunity()).isEqualTo(exam.getAttempts());
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowForAccessDeniedGetRequestDetails() throws Exception {
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final long proctorId = 2112;
        final UUID requestId = UUID.randomUUID();

        when(proctorUserDao.validateProctorSession(proctorId, sessionId, browserId)).thenReturn("Invalid");
        service.getTesteeRequestValues(sessionId, proctorId, browserId, requestId, true);
    }
}
