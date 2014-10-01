==============
 User Stories
==============

App lifecycle user stories
==========================

#. Perms offline during startup (no cache)

#. Perms offline during startup (cache)

#. Progman offline during startup (no cache)

#. Progman offline during startup (cache)

#. Deployment

#. Redeployment to running server

#. Database offline during startup

#. Memory leaks

#. Shutdown


Front-end login user stories
============================

#. Invalid credentials

#. Valid credentials, no role

#. Valid credentials, invalid role

#. Valid credentials, valid role, tenant not in good standing

#. Valid credentials, valid role, tenant not subscribed to TDS

#. Valid credentials, valid role, tenant subscribed (successful login)

#. Valid credentials, valid role, SSO offline

#. Valid credentials, valid role, progman offline (no cache)

#. Valid credentials, valid role, progman offline (cache)

#. Valid credentials, valid role, perms offline (no cache)

#. Valid credentials, valid role, perms offline (cache)

#. Valid credentials, valid role, Test Registration offline (no cache)

#. Valid credentials, valid role, Test Registration offline(cache)

#. Valid credentials, dual roles: only one has access

#. Valid credentials, parent domain not in good standing.

#. Existing SSO token from another application

#. Existing expired SSO token from another application

#. Existing expired SAML package.

#. Time out of sync

#. 

Session lifecycle user stories
==============================

#. SSO session ends when user ends login session (?)

#. Login session does not end when user ends SSO session

#. Login session times out appropriately

#. SSO session ends when login session times out.

#. What happens when session ends but app cannot reach SSO server?

#. What happens when tenancy state changes with paused test session?

#. What happens if app shuts down with active test session?

#. What happens if app shuts down with active login session/no active test session?

#. What happens if app shuts down with active login session/paused test session?

#. What happens if db connection lost with active login session?

Available test list user stories
================================

#. Single role: teacher can see interim assessments

#. Single role: teacher can see formative assessments

#. Single role: teach cannot see summative assessments

#. Single role: test administrator cannot see interim assessments

#. Single role: test administrator cannot see formative assessments

#. Single role: test administrator can see summative assessments

#. Double role: teacher/test administrator can see interim, formative and summative assessments.

#. Single role/double permissions: can see interim, formative and summative assessments.

#. Hierarchy: proctor cannot see tests belonging to sibling of role location

#. Hierarchy: proctor cannot see tests belonging to cousin of role location

#. Hierarchy: proctor can see tests belonging to child of role location?

#. Hierarchy: proctor cannot see tests belonging to parent of role location?

#. What happens when test registration information changes during a login session?

#. What number of tests and level of complexity of entity hierarchies can be supported and still maintain adequate performance for extracting the list of authorized tests?

#. If there are a lot of tests, does paging work?  How about search/filter/typeahead?

#. Hierarchy: SSO hierarchy is out-of-sync with Test Registration hierarchy

#. What happens when test registration goes offline during a session?

#. 