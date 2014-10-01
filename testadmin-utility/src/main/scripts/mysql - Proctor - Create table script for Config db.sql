create schema if not exists tdscore_dev_configs2012_sandbox default charset = UTF8;

/*
alter table `tds_fieldtestpriority`
	drop constraint `fk_tds_testeeatt`;

alter table `tds_coremessageuser`
	drop constraint `fk_msguser_msgobj`;

alter table `system_applicationsettings`
	drop constraint `tds_applicationsettings_client_applicationsettings`;

alter table `geo_clientapplication`
	drop constraint `fk_geoapp_db`;

alter table `client_testwindow`
	drop constraint `fk_timewindow`;

alter table `client_testtool`
	drop constraint `fk_clienttool_tooltype`;

alter table `client_tds_rtsattributevalues`
	drop constraint `fk_client_tds_rtsattributevalues_client_tds_rtsattribute`;

alter table `client_messagetranslation`
	drop constraint `fk_clientmsgtranslation`;

alter table `client_fieldtestpriority`
	drop constraint `fk_ft_testeeatt`;



drop table if exists `_messageid`;
drop table if exists `tds_testeerelationshipattribute`;
drop table if exists `tds_testeeattribute`;
drop table if exists `tds_testtooltype`;
drop table if exists `tds_testtoolrule`;
drop table if exists `tds_testtool`;
drop table if exists `tds_testproperties`;
drop table if exists `tds_systemflags`;
drop table if exists `tds_role`;
drop table if exists `tds_fieldtestpriority`;
drop table if exists `tds_coremessageuser`;
drop table if exists `tds_coremessageobject`;
drop table if exists `tds_configtype`;
drop table if exists `tds_clientaccommodationvalue`;
drop table if exists `tds_clientaccommodationtype`;
drop table if exists `tds_browserwhitelist`;
drop table if exists `tds_applicationsettings`;
drop table if exists `tds_application`;
drop table if exists `system_networkdiagnostics`;
drop table if exists `system_browserwhitelist`;
drop table if exists `system_applicationsettings`;
drop table if exists `statuscodes`;
drop table if exists `rts_role`;
drop table if exists `geo_database`;
drop table if exists `geo_clientapplication`;
drop table if exists `client_voicepack`;
drop table if exists `client_toolusage`;
drop table if exists `client_tooldependencies`;
drop table if exists `client_timewindow`;
drop table if exists `client_timelimits`;
drop table if exists `client_testscorefeatures`;
drop table if exists `client_testkey`;
drop table if exists `client_testformproperties`;
drop table if exists `client_testeerelationshipattribute`;
drop table if exists `client_testeeattribute`;
drop table if exists `client_test_itemtypes`;
drop table if exists `client_test_itemconstraint`;
drop table if exists `client_testwindow`;
drop table if exists `client_testtooltype`;
drop table if exists `client_testtoolrule`;
drop table if exists `client_testtool`;
drop table if exists `client_testrtsspecs`;
drop table if exists `client_testproperties`;
drop table if exists `client_testprerequisite`;
drop table if exists `client_testmode`;
drop table if exists `client_testgrades`;
drop table if exists `client_testeligibility`;
drop table if exists `client_tds_rtsattributevalues`;
drop table if exists `client_tds_rtsattribute`;
drop table if exists `client_systemflags`;
drop table if exists `client_subject`;
drop table if exists `client_server`;
drop table if exists `client_segmentproperties`;
drop table if exists `client_rtsroles`;
drop table if exists `client_pilotschools`;
drop table if exists `client_messagetranslationaudit`;
drop table if exists `client_messagetranslation`;
drop table if exists `client_messagearchive`;
drop table if exists `client_message`;
drop table if exists `client_language`;
drop table if exists `client_grade`;
drop table if exists `client_forbiddenappslist`;
drop table if exists `client_forbiddenappsexcludeschools`;
drop table if exists `client_fieldtestpriority`;
drop table if exists `client_externs`;
drop table if exists `client_allowips`;
drop table if exists `client_accommodations`;
drop table if exists `client_accommodationfamily`;
drop table if exists `client`;

*/

create table `client`  ( 
	`name`            	varchar(100) not null,
	`origin`          	varchar(50) null,
	`internationalize`	bit not null default 1,
	`defaultlanguage` 	varchar(50) not null default 'enu',
	constraint `pk_client` primary key clustered(`name`)
) default charset = UTF8;

create table `client_accommodationfamily`  ( 
	`clientname`	varchar(100) not null,
	`family`    	varchar(50) not null,
	`label`     	varchar(200) not null,
	constraint `pk_accomfamily` primary key clustered(`clientname`,`family`)
) default charset = UTF8;

create table `client_accommodations`  ( 
	`clientname`      	varchar(100) not null,
	`type`            	varchar(255) not null,
	`code`            	varchar(255) not null,
	`value`           	varchar(255) not null,
	`isdefault`       	bit not null,
	`allowcombine`    	bit not null,
	`allowchange`     	bit not null,
	`isselectable`    	bit not null,
	`isvisible`       	bit not null default 1,
	`valuedescription`	varchar(500) null,
	`tideselectable`  	bit null,
	`rtsfieldname`    	varchar(100) not null,
	constraint `pk_clientaccommodations` primary key clustered(`clientname`,`code`)
) default charset = UTF8;

create table `client_allowips`  ( 
	`clientname`     	varchar(100) not null,
	`applicationname`	varchar(50) not null,
	`allowip`        	varchar(25) not null,
	`datebegin`      	datetime(3) not null,
	`dateend`        	datetime(3) not null,
	`comments`       	varchar(255) null,
	`userid`         	varchar(50) null,
	`environment`    	varchar(100) not null,
	`message`        	text null,
	`datecreated`    	datetime(3) null, -- constraint `df_client_allowips_datecreated`  default (getdate()),
	constraint `pk_client_allowips` primary key clustered(`clientname`,`applicationname`,`allowip`,`environment`)
) default charset = UTF8;

create table `client_externs`  ( 
	`_key`              	varbinary(16) not null, -- constraint `df_client_externs__key`  default (newid()),
	`testeedb`          	varchar(255) not null,
	`proctordb`         	varchar(255) not null,
	`testdb`            	varchar(255) not null,
	`scorerdb`          	varchar(255) null,
	`archivedb`         	varchar(255) null,
	`testeetype`        	varchar(255) not null default 'rts',
	`proctortype`       	varchar(255) not null default 'rts',
	`scorertype`        	varchar(255) not null default 'rts',
	`sessiondb`         	varchar(255) null,
	`errorsserver`      	varchar(255) null,
	`errorsdb`          	varchar(255) null,
	`clientname`        	varchar(100) not null,
	`bankadminkey`      	varchar(150) null,
	`clientstylepath`   	varchar(500) null,
	`qaxmlpath`         	varchar(500) null,
	`environment`       	varchar(100) not null,
	`ispracticetest`    	bit not null,
	`timezoneoffset`    	int not null default 0,
	`publishurl`        	varchar(255) null,
	`initialreportingid`	bigint not null default 1,
	`initialsessionid`  	bigint not null default 1,
	`qabrokerguid`      	varbinary(16) null,
	`expiresatminutes`  	int null,
	constraint `pk_client_externs` primary key clustered(`_key`)
) default charset = UTF8;

create table `client_fieldtestpriority`  ( 
	`tds_id`    	varchar(50) not null,
	`clientname`	varchar(100) not null,
	`priority`  	int not null,
	`testid`    	varchar(200) not null,
	constraint `pk_clientft` primary key clustered(`clientname`,`testid`,`tds_id`,`priority`)
) default charset = UTF8;

create table `client_forbiddenappsexcludeschools`  ( 
	`districtname`	varchar(150) null,
	`districtid`  	varchar(150) not null,
	`schoolname`  	varchar(150) null,
	`schoolid`    	varchar(150) not null,
	`clientname`  	varchar(100) not null,
	constraint `pk_client_forbiddenappsexcludeschools` primary key clustered(`districtid`,`schoolid`,`clientname`)
) default charset = UTF8;

create table `client_forbiddenappslist`  ( 
	`os_id`             	varchar(25) not null,
	`processname`       	varchar(150) not null,
	`processdescription`	varchar(150) null,
	`clientname`        	varchar(100) not null,
	constraint `pk_client_forbiddenappslist` primary key clustered(`os_id`,`processname`,`clientname`)
) default charset = UTF8;

create table `client_grade`  ( 
	`gradecode` 	varchar(25) not null,
	`grade`     	varchar(64) not null,
	`clientname`	varchar(100) not null,
	`origin`    	varchar(50) null, -- constraint `df__client_gr__origi__5d56b96f`  default (db_name()),
	constraint `pk_client_grade` primary key clustered(`gradecode`,`clientname`)
) default charset = UTF8;

create table `client_language`  ( 
	`clientname`  	varchar(100) not null,
	`language`    	varchar(100) not null,
	`languagecode`	varchar(25) not null,
	`origin`      	varchar(50) null, -- constraint `df__client_la__origi__5f3f01e1`  default (db_name()),
	constraint `pk_client_language` primary key clustered(`clientname`,`languagecode`)
) default charset = UTF8;

create table `client_message`  ( 
	`clientname`    	varchar(100) not null,
	`messageid`     	int not null,
	`translationid` 	int not null default 0,
	`systemid`      	varchar(25) not null,
	`contexttype`   	varchar(25) not null,
	`context`       	varchar(100) not null,
	`defaultmessage`	varchar(255) not null,
	`clientmessage` 	text null,
	`paralabels`    	varchar(255) null,
	`languagecode`  	varchar(25) null default 'enu',
	`gradecode`     	varchar(25) null,
	`subject`       	varchar(100) null,
	`priority`      	int not null default 0,
	`usefilepath`   	bit not null default 0,
	`datechanged`   	datetime(3) not null, -- constraint `df_client_message_datechanged`  default (getdate()),
	`datepublished` 	datetime(3) null,
	constraint `pk_client_message` primary key clustered(`clientname`,`messageid`,`translationid`,`systemid`)
) default charset = UTF8;

create table `client_messagearchive`  ( 
	`clientname`    	varchar(100) not null,
	`messageid`     	int not null,
	`translationid` 	int not null default 0,
	`systemid`      	varchar(25) not null,
	`contexttype`   	varchar(25) not null,
	`context`       	varchar(100) not null,
	`defaultmessage`	varchar(255) not null,
	`clientmessage` 	text null,
	`paralabels`    	varchar(255) null,
	`languagecode`  	varchar(25) null,
	`gradecode`     	varchar(25) null,
	`subject`       	varchar(100) null,
	`priority`      	int not null  default 0,
	`usefilepath`   	bit not null  default 0,
	`datechanged`   	datetime(3) not null, -- constraint `df__client_me__datec__3c89f72a`  default (getdate()),
	`datepublished` 	datetime(3) null,
	`_date`         	datetime(3) not null, -- constraint `df__client_me___date__3d7e1b63`  default (getdate()),
	`username`      	varchar(128) null,
	`action`        	varchar(128) null,
	constraint `pk_client_messagearchive` primary key clustered(`clientname`,`messageid`,`translationid`,`systemid`,`_date`)
) default charset = UTF8;

create table `client_messagetranslation`  ( 
	`_fk_coremessageobject`	bigint not null,
	`client`               	varchar(100) not null,
	`message`              	text not null,
	`language`             	varchar(30) not null,
	`grade`                	varchar(25) not null default '--any--',
	`subject`              	varchar(50) not null default '--any--',
	`_key`                 	varbinary(16) not null, -- constraint `df__client_mes___key__1293bd5e`  default (newid()),
	`datealtered`          	datetime(3) null,
	constraint `pk_clientmsgtranslation` primary key clustered(`_key`)
) default charset = UTF8;

create table `client_messagetranslationaudit`  ( 
	`_fk_coremessageobject`	bigint not null,
	`context`              	varchar(100) not null,
	`contexttype`          	varchar(50) not null,
	`client`               	varchar(100) not null,
	`appkey`               	varchar(255) not null,
	`message`              	text null,
	`messageid`            	int not null,
	`language`             	varchar(30) not null,
	`grade`                	varchar(25) not null,
	`subject`              	varchar(50) not null,
	`datealtered`          	datetime(3) not null,
	`username`             	varchar(128) not null,
	`_date`                	datetime(3) not null,
	`action`               	varchar(128) not null 
) default charset = UTF8;

create table `client_pilotschools`  ( 
	`clientname` 	varchar(100) not null,
	`_efk_testid`	varchar(50) not null,
	`schoolid`   	varchar(50) not null,
	`schoolname` 	varchar(100) null,
	constraint `pk_client_pilotschools` primary key clustered(`clientname`,`_efk_testid`,`schoolid`)
) default charset = UTF8;

create table `client_rtsroles`  ( 
	`tds_role`     	varchar(100) not null,
	`rts_role`     	varchar(100) not null,
	`clientname`   	varchar(100) not null,
	`datechanged`  	datetime(3) null, -- default (getdate()),
	`datepublished`	datetime(3) null,
	`sessiontype`  	int not null default 0,
	constraint `pk_clientrtsroles` primary key clustered(`clientname`,`tds_role`,`rts_role`,`sessiontype`)
) default charset = UTF8;

create table `client_segmentproperties`  ( 
	`ispermeable`    	int not null,
	`clientname`     	varchar(100) not null,
	`entryapproval`  	int not null,
	`exitapproval`   	int not null,
	`itemreview`     	bit not null default 1,
	`segmentid`      	varchar(255) not null,
	`segmentposition`	int not null,
	`parenttest`     	varchar(255) not null,
	`ftstartdate`    	datetime(3) null,
	`ftenddate`      	datetime(3) null,
	`label`          	varchar(255) null,
	`modekey`        	varchar(250) null,
	constraint `pk_segmentprops` primary key clustered(`clientname`,`parenttest`,`segmentid`)
) default charset = UTF8;

create table `client_server`  ( 
	`_key`           	varbinary(16) not null, -- constraint `df_client_server__key`  default (newid()),
	`clientname`     	varchar(100) not null,
	`applicationname`	varchar(100) not null,
	`serverip`       	varchar(100) not null,
	`serverid`       	varchar(100) not null,
	`servertype`     	varchar(100) not null,
	`virtualdirname` 	varchar(50) null,
	constraint `pk_client_server` primary key clustered(`_key`)
) default charset = UTF8;

create table `client_subject`  ( 
	`subjectcode`	varchar(25) null,
	`subject`    	varchar(100) not null,
	`clientname` 	varchar(100) not null,
	`origin`     	varchar(50) null, -- constraint `df__client_su__origi__61274a53`  default (db_name(,
	constraint `pk_client_subject_1` primary key clustered(`subject`,`clientname`)
) default charset = UTF8;

create table `client_systemflags`  ( 
	`auditobject`   	varchar(255) not null,
	`ison`          	int not null,
	`description`   	varchar(255) null,
	`clientname`    	varchar(100) not null,
	`ispracticetest`	bit not null,
	`datechanged`   	datetime(3) null, -- constraint `df__client_sy__datec__3e1d39e1`  default (getdate()),
	`datepublished` 	datetime(3) null,
	constraint `pk_clientsystemflags` primary key clustered(`clientname`,`auditobject`,`ispracticetest`)
) default charset = UTF8;

create table `client_tds_rtsattribute`  ( 
	`clientname`     	varchar(100) not null,
	`name`           	varchar(50) not null,
	`fieldname`      	varchar(35) not null,
	`datatype`       	varchar(50) null default 'codelist',
	`_efk_entitytype`	bigint not null default 6,
	`type`           	varchar(25) not null default 'attribute',
	`entitytype`     	varchar(100) null,
	constraint `pk_rts_attribute` primary key clustered(`clientname`,`fieldname`,`_efk_entitytype`,`type`)
) default charset = UTF8;

create table `client_tds_rtsattributevalues`  ( 
	`clientname`     	varchar(100) not null,
	`fieldname`      	varchar(35) not null,
	`value`          	varchar(365) not null,
	`allowcombine`   	bit not null default 1,
	`_efk_entitytype`	bigint not null  default 6,
	`type`           	varchar(25) not null default 'attribute',
	constraint `pk_rts_attributevalues` primary key clustered(`clientname`,`fieldname`,`value`,`_efk_entitytype`,`type`)
) default charset = UTF8;

create table `client_testeligibility`  ( 
	`_key`           	varbinary(16) not null, -- default (newid()),
	`clientname`     	varchar(100) not null,
	`testid`         	varchar(150) not null,
	`rtsname`        	varchar(100) not null,
	`enables`        	bit not null,
	`disables`       	bit not null,
	`rtsvalue`       	varchar(400) not null,
	`_efk_entitytype`	bigint not null,
	`eligibilitytype`	varchar(50) null,
	`matchtype`      	int not null default 0,
	constraint `pk_client_testeligibility` primary key clustered(`_key`)
) default charset = UTF8;

create table `client_testgrades`  ( 
	`clientname`       	varchar(100) not null,
	`testid`           	varchar(150) not null,
	`grade`            	varchar(25) not null,
	`requireenrollment`	bit not null default 0,
	`enrolledsubject`  	varchar(100) null,
	constraint `pk_testgrades` primary key clustered(`clientname`,`testid`,`grade`)
) default charset = UTF8;

create table `client_testmode`  ( 
	`clientname`            	varchar(100) not null,
	`testid`                	varchar(200) not null,
	`mode`                  	varchar(50) not null default 'online',
	`algorithm`             	varchar(50) null,
	`formtideselectable`    	bit not null default 0,
	`issegmented`           	bit not null default 0,
	`maxopps`               	int not null default 50,
	`requirertsform`        	bit not null default 0,
	`requirertsformwindow`  	bit not null default 0,
	`requirertsformifexists`	bit not null default 1,
	`sessiontype`           	int not null default -1,
	`testkey`               	varchar(250) null,
	`_key`                  	varbinary(16) not null, -- constraint `df_client_testmode__key`  default (newid()),
	constraint `pk_client_testmode` primary key clustered(`_key`)
) default charset = UTF8;

create table `client_testprerequisite`  ( 
	`clientname`  	varchar(100) not null,
	`isactive`    	bit not null default 1,
	`prereqtestid`	varchar(255) not null,
	`testid`      	varchar(255) not null,
	`_key`        	varbinary(16) not null, -- constraint `df_client_testprerequisite__key`  default (newid()),
	constraint `pk_client_testprerequisite` primary key clustered(`_key`)
) default charset = UTF8;

create table `client_testproperties`  ( 
	`clientname`             	varchar(100) not null,
	`testid`                 	varchar(255) not null,
	`maxopportunities`       	int null,
	`handscoreproject`       	int null,
	`prefetch`               	int not null default 2,
	`datechanged`            	datetime(3) null, -- constraint `df_client_testproperties_datechanged`  default (getdate()),
	`isprintable`            	bit not null default 0,
	`isselectable`           	bit not null default 1,
	`label`                  	varchar(255) null,
	`printitemtypes`         	varchar(1000) null default '',
	`scorebytds`             	bit not null default 1,
	`batchmodereport`        	bit not null default 0,
	`subjectname`            	varchar(100) null,
	`origin`                 	varchar(50) null, -- constraint `df__client_te__origi__51e506c3`  default (db_name()),
	`source`                 	varchar(50) null, -- constraint `df__client_te__sourc__52d92afc`  default (db_name()),
	`maskitemsbysubject`     	bit not null default 1,
	`initialabilitybysubject`	bit not null default 1,
	`startdate`              	datetime(3) null,
	`enddate`                	datetime(3) null,
	`ftstartdate`            	datetime(3) null,
	`ftenddate`              	datetime(3) null,
	`accommodationfamily`    	varchar(50) null,
	`sortorder`              	int null,
	`rtsformfield`           	varchar(50) not null default 'tds-testform',
	`rtswindowfield`         	varchar(50) not null default 'tds-testwindow',
	`windowtideselectable`   	bit not null default 0,
	`requirertswindow`       	bit not null default 0,
	`reportinginstrument`    	varchar(50) null,
	`tide_id`                	varchar(100) null,
	`forcecomplete`          	bit not null default 1,
	`rtsmodefield`           	varchar(50) not null default 'tds-testmode',
	`modetideselectable`     	bit not null default 0,
	`requirertsmode`         	bit not null default 0,
	`requirertsmodewindow`   	bit not null default 0,
	`deleteunanswereditems`  	bit not null default 0,
	`abilityslope`           	float not null default 1,
	`abilityintercept`       	float not null default 0,
	`validatecompleteness`   	bit not null default 0,
	`gradetext`              	varchar(50) null,
	constraint `pk_client_testproperties` primary key clustered(`clientname`,`testid`)
) default charset = UTF8;

create table `client_testrtsspecs`  ( 
	`_key`        	varbinary(16) not null, -- default (newid()),
	`clientname`  	varchar(100) not null,
	`testid`      	varchar(150) not null,
	`rtsfieldname`	varchar(50) not null,
	`enables`     	bit not null,
	`disables`    	bit not null,
	`attrvalues`  	text not null,
	constraint `pk_client_testrtsspecs` primary key clustered(`_key`)
) default charset = UTF8;

create table `client_testtool`  ( 
	`clientname`          	varchar(100) not null,
	`type`                	varchar(255) not null,
	`code`                	varchar(255) not null,
	`value`               	varchar(255) not null,
	`isdefault`           	bit not null,
	`allowcombine`        	bit not null,
	`valuedescription`    	varchar(255) null,
	`context`             	varchar(255) not null,
	`sortorder`           	int not null default 0,
	`origin`              	varchar(50) null, -- constraint `df__client_te__origi__579de019`  default (db_name()),
	`source`              	varchar(50) null, -- constraint `df__client_te__sourc__58920452`  default (db_name()),
	`contexttype`         	varchar(50) not null,
	`testmode`            	varchar(25) not null default 'all',
	`equivalentclientcode`	varchar(255) null,
	constraint `pk_client_toolvalues` primary key clustered(`clientname`,`context`,`contexttype`,`type`,`code`)
) default charset = UTF8;

create table `client_testtoolrule`  ( 
	`clientname`	varchar(100) not null,
	`testid`    	varchar(200) not null,
	`tooltype`  	varchar(255) not null,
	`toolcode`  	varchar(255) not null,
	`rule`      	varchar(100) not null,
	`rulevalue` 	varchar(255) not null,
	constraint `pk_clienttoolrule` primary key clustered(`clientname`,`testid`,`toolcode`,`rule`)
) default charset = UTF8;

create table `client_testtooltype`  ( 
	`clientname`             	varchar(100) not null,
	`toolname`               	varchar(255) not null,
	`allowchange`            	bit not null default 1,
	`tideselectable`         	bit null,
	`rtsfieldname`           	varchar(100) null,
	`isrequired`             	bit not null default 0,
	`tideselectablebysubject`	bit not null default 0,
	`isselectable`           	bit not null default 1,
	`isvisible`              	bit not null default 1,
	`studentcontrol`         	bit not null default 1,
	`tooldescription`        	varchar(255) null,
	`sortorder`              	int not null default 0,
	`dateentered`            	datetime(3) not null, -- constraint `df__client_te__datee__39e294a9`  default (getdate()),
	`origin`                 	varchar(50) null, -- constraint `df__client_te__origi__54c1736e`  default (db_name()),
	`source`                 	varchar(50) null, -- constraint `df__client_te__sourc__55b597a7`  default (db_name()),
	`contexttype`            	varchar(50) not null,
	`context`                	varchar(255) not null,
	`dependsontooltype`      	varchar(50) null,
	`disableonguestsession`  	bit not null default 0,
	`isfunctional`           	bit not null default 1,
	`testmode`               	varchar(25) not null default 'all',
	constraint `pk_clienttesttool` primary key clustered(`clientname`,`context`,`contexttype`,`toolname`)
) default charset = UTF8;

create table `client_testwindow`  ( 
	`clientname` 	varchar(100) not null,
	`testid`     	varchar(200) not null,
	`window`     	int not null default 1,
	`numopps`    	int not null,
	`startdate`  	datetime(3) null,
	`enddate`    	datetime(3) null,
	`origin`     	varchar(100) not null, -- default (db_name()),
	`source`     	varchar(100) not null, -- constraint `df__client_te__sourc__2739d489`  default (db_name()),
	`windowid`   	varchar(50) null,
	`_key`       	varbinary(16) not null, -- constraint `df__client_tes___key__382f5661`  default (newid()),
	`sessiontype`	int not null default -1,
	`sortorder`  	int null default 1,
	constraint `pk_testwindow` primary key clustered(`_key`)
) default charset = UTF8;

create table `client_test_itemconstraint`  ( 
	`clientname`	varchar(100) not null,
	`testid`    	varchar(255) not null,
	`propname`  	varchar(100) not null,
	`propvalue` 	varchar(100) not null,
	`tooltype`  	varchar(255) not null,
	`toolvalue` 	varchar(255) not null,
	`item_in`   	bit not null,
	constraint `pk_itemconstraint` primary key clustered(`clientname`,`testid`,`propname`,`propvalue`,`item_in`)
) default charset = UTF8;

create table `client_test_itemtypes`  ( 
	`clientname`	varchar(100) not null,
	`testid`    	varchar(255) not null,
	`itemtype`  	varchar(25) not null,
	`origin`    	varchar(50) null, -- default (db_name()),
	`source`    	varchar(50) null, -- default (db_name()),
	constraint `pk_itemtypes` primary key clustered(`clientname`,`testid`,`itemtype`)
) default charset = UTF8;

create table `client_testeeattribute`  ( 
	`rtsname`      	varchar(50) not null,
	`tds_id`       	varchar(50) not null,
	`clientname`   	varchar(100) not null,
	`reportname`   	varchar(100) null,
	`type`         	varchar(50) not null,
	`label`        	varchar(50) null,
	`atlogin`      	varchar(25) null,
	`sortorder`    	int null,
	`islatencysite`	bit not null default 0,
	constraint `pk_testeeattribute` primary key clustered(`clientname`,`tds_id`)
) default charset = UTF8;

create table `client_testeerelationshipattribute`  ( 
	`clientname`      	varchar(50) not null,
	`relationshiptype`	varchar(50) not null,
	`rtsname`         	varchar(50) not null,
	`tds_id`          	varchar(50) not null,
	`label`           	varchar(50) null,
	`atlogin`         	varchar(25) null,
	`sortorder`       	int null,
	`reportname`      	varchar(50) null,
	constraint `pk_client_testeerelationshipattribute` primary key clustered(`clientname`,`tds_id`)
) default charset = UTF8;

create table `client_testformproperties`  ( 
	`clientname`    	varchar(100) not null,
	`_efk_testform` 	varchar(50) not null,
	`startdate`     	datetime(3) null,
	`enddate`       	datetime(3) null,
	`language`      	varchar(25) not null,
	`formid`        	varchar(200) null,
	`testid`        	varchar(150) not null,
	`testkey`       	varchar(250) null,
	`clientformid`  	varchar(25) null,
	`accommodations`	text null,
	constraint `pk_testform` primary key clustered(`clientname`,`_efk_testform`)
) default charset = UTF8;

create table `client_testkey`  ( 
	`clientname`       	varchar(100) not null,
	`testid`           	varchar(255) not null,
	`_efk_adminsubject`	varchar(250) not null,
	`isactive`         	bit not null,
	`_date`            	datetime(3) not null, -- constraint `df__client_te___date__236943a5`  default (getdate()),
	`source`           	varchar(100) not null, -- constraint `df__client_te__sourc__245d67de`  default (db_name()),
	`origin`           	varchar(100) not null, -- constraint `df__client_te__origi__25518c17`  default (db_name()),
	constraint `pk_client_testkey` primary key clustered(`clientname`,`testid`,`_efk_adminsubject`)
) default charset = UTF8;

create table `client_testscorefeatures`  ( 
	`clientname`           	varchar(100) not null,
	`testid`               	varchar(255) not null,
	`measureof`            	varchar(250) not null,
	`measurelabel`         	varchar(200) not null,
	`reporttostudent`      	bit not null default 0,
	`reporttoproctor`      	bit not null default 0,
	`reporttoparticipation`	bit not null default 0,
	`useforability`        	bit not null default 0,
	`reportlabel`          	text null,
	`reportorder`          	int null,
	constraint `pk_scorefeatures` primary key clustered(`clientname`,`testid`,`measureof`,`measurelabel`)
) default charset = UTF8;

create table `client_timelimits`  ( 
	`_key`                   	varbinary(16) not null, -- constraint `df_client_timelimits__key`  default (newid()),
	`_efk_testid`            	varchar(255) null,
	`oppexpire`              	int not null,
	`opprestart`             	int not null,
	`oppdelay`               	int not null,
	`interfacetimeout`       	int null,
	`clientname`             	varchar(100) null,
	`ispracticetest`         	bit not null default 0,
	`refreshvalue`           	int null,
	`tainterfacetimeout`     	int null,
	`tacheckintime`          	int null,
	`datechanged`            	datetime(3) null, -- constraint `df__client_ti__datec__3d2915a8`  default (getdate()),
	`datepublished`          	datetime(3) null,
	`environment`            	varchar(100) null,
	`sessionexpire`          	int not null default 8,
	`requestinterfacetimeout`	int not null default 120,
	`refreshvaluemultiplier` 	int not null default 2,
	constraint `pk_client_timelimits` primary key clustered(`_key`)
) default charset = UTF8;

create table `client_timewindow`  ( 
	`clientname` 	varchar(100) not null,
	`windowid`   	varchar(50) not null,
	`startdate`  	datetime(3) null,
	`enddate`    	datetime(3) null,
	`description`	varchar(200) null,
	constraint `pk_timewindow` primary key clustered(`clientname`,`windowid`)
) default charset = UTF8;

create table `client_tooldependencies`  ( 
	`context`    	varchar(250) not null,
	`contexttype`	varchar(50) not null,
	`iftype`     	varchar(50) not null,
	`ifvalue`    	varchar(255) not null,
	`isdefault`  	bit not null default 0,
	`thentype`   	varchar(50) not null,
	`thenvalue`  	varchar(255) not null,
	`clientname` 	varchar(100) not null,
	`_key`       	varbinary(16) not null, -- constraint `df_client_tooldependencies__key`  default (newid()),
	`testmode`   	varchar(25) not null default 'all',
	constraint `pk_client_tooldependencies` primary key clustered(`_key`)
) default charset = UTF8;

create table `client_toolusage`  ( 
	`clientname` 	varchar(100) not null,
	`testid`     	varchar(150) not null,
	`tooltype`   	varchar(100) not null,
	`recordusage`	bit not null,
	`reportusage`	bit not null,
	constraint `pk_toolusage` primary key clustered(`clientname`,`testid`,`tooltype`)
) default charset = UTF8;

create table `client_voicepack`  ( 
	`os_id`        	varchar(25) not null,
	`voicepackname`	varchar(255) not null,
	`priority`     	int not null,
	`languagecode` 	varchar(25) not null,
	`clientname`   	varchar(100) not null,
	constraint `pk_client_voicepack` primary key clustered(`os_id`,`voicepackname`,`clientname`)
) default charset = UTF8;

create table `geo_clientapplication`  ( 
	`clientname`      	varchar(100) not null,
	`environment`     	varchar(100) not null,
	`url`             	varchar(200) null,
	`appname`         	varchar(100) not null,
	`servicetype`     	varchar(50) not null,
	`_fk_geo_database`	varbinary(16) not null,
	`_key`            	varbinary(16) not null, -- constraint `df_geo_clientapplication__key`  default (newid()),
	constraint `pk__geo_clientapplic` primary key clustered(`_key`)
) default charset = UTF8;

create table `geo_database`  ( 
	`servername`	varchar(100) not null,
	`dbname`    	varchar(100) not null,
	`brokerguid`	varbinary(16) null,
	`_key`      	varbinary(16) not null,
	`tds_id`    	varchar(25) null,
	constraint `pk_geodatabase` primary key clustered(`_key`)
) default charset = UTF8;

create table `rts_role`  ( 
	`clientname` 	varchar(100) not null,
	`rolename`   	varchar(100) not null,
	`description`	varchar(100) null,
	constraint `pk_rts_role` primary key clustered(`clientname`,`rolename`)
) default charset = UTF8;

create table `statuscodes`  ( 
	`usage`      	varchar(50) not null,
	`status`     	varchar(50) not null,
	`description`	varchar(255) null,
	`stage`      	varchar(50) null,
	`_key`       	varbinary(16) not null, -- constraint `df_statuscodes__key`  default (newid()),
	constraint `pk_statuscodes` primary key clustered(`_key`)
) default charset = UTF8;

create table `system_applicationsettings`  ( 
	`clientname`                 	varchar(100) not null,
	`environment`                	varchar(100) not null,
	`appname`                    	varchar(100) not null,
	`property`                   	varchar(100) not null,
	`type`                       	varchar(100) not null,
	`value`                      	varchar(100) not null,
	`_fk_tds_applicationsettings`	varbinary(16) not null,
	constraint `pk_systemapplicationsetting` primary key clustered(`clientname`,`_fk_tds_applicationsettings`)
) default charset = UTF8;

create table `system_browserwhitelist`  ( 
	`clientname`              	varchar(100) not null,
	`appname`                 	varchar(100) not null,
	`environment`             	varchar(100) not null,
	`browsername`             	varchar(100) not null,
	`osname`                  	varchar(100) null,
	`hw_arch`                 	varchar(100) null,
	`browsermaxversion`       	float null,
	`action`                  	varchar(150) null,
	`priority`                	int null,
	`osmaxversion`            	float null,
	`_fk_tds_browserwhitelist`	varbinary(16) not null,
	`browserminversion`       	float null,
	`osminversion`            	float null,
	`messagekey`              	varchar(255) null,
	constraint `pk_systembrowserwhitelist_tdsbrowserwhitelist` primary key clustered(`clientname`,`_fk_tds_browserwhitelist`)
) default charset = UTF8;

create table `system_networkdiagnostics`  ( 
	`clientname`     	varchar(100) not null,
	`appname`        	varchar(100) not null,
	`testlabel`      	varchar(100) not null,
	`mindataratereqd`	int null,
	`aveitemsize`    	int null,
	`responsetime`   	int null,
	`_key`           	varbinary(16) not null, -- default (newid()),
	constraint `pk_system_networkdiagnostics` primary key clustered(`_key`)
) default charset = UTF8;

create table `tds_application`  ( 
	`_key`       	varbinary(16) not null, -- constraint `df_tds_application__key`  default (newid()),
	`name`       	varchar(50) not null,
	`description`	varchar(255) not null,
	constraint `pk_tds_application` primary key clustered(`_key`)
) default charset = UTF8;

create table `tds_applicationsettings`  ( 
	`_key`         	varbinary(16) not null, -- constraint `df__tds_applic___key__573ded66`  default (newid(,
	`environment`  	varchar(100) not null,
	`appname`      	varchar(100) not null,
	`property`     	varchar(100) not null,
	`type`         	varchar(100) not null,
	`value`        	varchar(100) not null,
	`isoperational`	bit not null default 1,
	constraint `pk_tds_applicationsettings` primary key clustered(`_key`)
) default charset = UTF8;

create table `tds_browserwhitelist`  ( 
	`_key`             	varbinary(16) not null, -- constraint `df__tds_browse___key__5b0e7e4a`  default (newid()),
	`appname`          	varchar(100) not null,
	`environment`      	varchar(100) not null,
	`browsername`      	varchar(100) not null,
	`osname`           	varchar(100) null,
	`hw_arch`          	varchar(100) null,
	`browserminversion`	float null,
	`browsermaxversion`	float null,
	`action`           	varchar(150) null,
	`priority`         	int null,
	`osminversion`     	float null,
	`isoperational`    	bit null,
	`osmaxversion`     	float null,
	`messagekey`       	varchar(255) null,
	constraint `pk_tds_browserwhitelist` primary key clustered(`_key`)
) default charset = UTF8;

create table `tds_clientaccommodationtype`  ( 
	`toolname`               	varchar(255) not null,
	`clientname`             	varchar(100) not null,
	`allowchange`            	bit not null,
	`tideselectable`         	bit null,
	`rtsfieldname`           	varchar(100) null,
	`isrequired`             	bit not null,
	`tideselectablebysubject`	bit not null,
	constraint `pk_clientacctype` primary key clustered(`toolname`,`clientname`)
) default charset = UTF8;

create table `tds_clientaccommodationvalue`  ( 
	`clientname`      	varchar(100) not null,
	`type`            	varchar(255) not null,
	`code`            	varchar(255) not null,
	`value`           	varchar(255) not null,
	`isdefault`       	bit not null,
	`valuedescription`	varchar(255) null,
	`allowcombine`    	bit not null,
	`allowadd`        	bit not null,
	`sortorder`       	int not null,
	constraint `pk_clientaccvalue` primary key clustered(`clientname`,`code`)
) default charset = UTF8;

create table `tds_configtype`  ( 
	`configtype`     	varchar(25) not null,
	`pagename`       	varchar(50) not null,
	`pagedescription`	varchar(100) null,
	`pagepath`       	varchar(100) not null,
	`_key`           	varbinary(16) not null, -- constraint `df_tds_configtype__key`  default (newid()),
	constraint `pk_tds_configtype` primary key clustered(`_key`)
) default charset = UTF8;

create table `tds_coremessageobject`  ( 
	`context`            	varchar(100) not null,
	`contexttype`        	varchar(50) not null,
	`messageid`          	int not null,
	`ownerapp`           	varchar(100) not null,
	`appkey`             	varchar(255) not null,
	`message`            	text null,
	`paralabels`         	varchar(255) null,
	`_key`               	bigint not null auto_increment,
	`fromclient`         	varchar(100) null,
	`datealtered`        	datetime(3) null,
	`nodes`              	text null,
	`ismessageshowtouser`	bit null,
	constraint `pk_coremsgobject` primary key clustered(`_key`)
) default charset = UTF8;

create table `tds_coremessageuser`  ( 
	`_fk_coremessageobject`	bigint not null,
	`systemid`             	varchar(100) not null,
	constraint `pk_msguser` primary key clustered(`_fk_coremessageobject`,`systemid`)
) default charset = UTF8;

create table `tds_fieldtestpriority`  ( 
	`tds_id`  	varchar(50) not null,
	`priority`	int not null,
	constraint `pk_tds_ftattribute` primary key clustered(`tds_id`,`priority`)
) default charset = UTF8;

create table `tds_role`  ( 
	`rolename`   	varchar(100) not null,
	`description`	varchar(100) null,
	`_key`       	varbinary(16) not null, -- constraint `df_tds_role__key`  default (newid()),
	constraint `pk_tds_role` primary key clustered(`_key`)
) default charset = UTF8;

create table `tds_systemflags`  ( 
	`auditobject`   	varchar(255) not null,
	`description`   	varchar(255) null,
	`ison`          	int not null,
	`ispracticetest`	bit not null,
	`_key`          	varbinary(16) not null, -- constraint `df_tds_systemflags__key`  default (newid(,
	constraint `pk_tds_systemflags` primary key clustered(`_key`)
) default charset = UTF8;

create table `tds_testproperties`  ( 
	`fieldname`  	varchar(100) not null,
	`description`	varchar(255) null,
	`isrequired` 	bit not null,
	`_key`       	varbinary(16) not null, -- constraint `df_tds_testproperties__key`  default (newid(,
	constraint `pk_tds_testproperties` primary key clustered(`_key`)
) default charset = UTF8;

create table `tds_testtool`  ( 
	`type`            	varchar(255) not null,
	`code`            	varchar(255) not null,
	`value`           	varchar(255) not null,
	`isdefault`       	bit not null,
	`valuedescription`	varchar(255) null,
	`allowcombine`    	bit not null,
	`allowadd`        	bit not null,
	`sortorder`       	int not null,
	constraint `pk_testtool` primary key clustered(`code`)
) default charset = UTF8;

create table `tds_testtoolrule`  ( 
	`tooltype`	varchar(255) not null,
	`toolcode`	varchar(255) not null,
	`rule`    	varchar(100) not null,
	constraint `pk_toolrule` primary key clustered(`tooltype`,`toolcode`,`rule`)
) default charset = UTF8;

create table `tds_testtooltype`  ( 
	`toolname`               	varchar(255) not null,
	`allowchange`            	bit not null,
	`tideselectable`         	bit null,
	`rtsfieldname`           	varchar(100) null,
	`isrequired`             	bit not null,
	`tideselectablebysubject`	bit not null,
	constraint `pk_tooltype` primary key clustered(`toolname`)
) default charset = UTF8;

create table `tds_testeeattribute`  ( 
	`rtsname`      	varchar(50) not null,
	`tds_id`       	varchar(50) not null,
	`type`         	varchar(50) not null,
	`label`        	varchar(50) null,
	`atlogin`      	varchar(25) null,
	`sortorder`    	int null,
	`reportname`   	varchar(50) null,
	`islatencysite`	bit not null default 0,
	constraint `pk_tds_testeeattribute` primary key clustered(`tds_id`)
) default charset = UTF8;

create table `tds_testeerelationshipattribute`  ( 
	`relationshiptype`	varchar(50) not null,
	`rtsname`         	varchar(50) not null,
	`tds_id`          	varchar(50) not null,
	`label`           	varchar(50) null,
	`atlogin`         	varchar(25) null,
	`sortorder`       	int null,
	`reportname`      	varchar(50) null,
	constraint `pk_tds_testeerelationshipattribute` primary key clustered(`tds_id`)
) default charset = UTF8;

create table `_messageid`  ( 
	`_key`	int not null auto_increment,
	`date`	datetime(3) null,
	constraint `pk__messageid` primary key clustered(`_key`)
) default charset = UTF8;

-- identity(10000,1)
alter table `_messageid` auto_increment = 10000;

create unique index `ix_clientexterns`
	on `client_externs`(`clientname`, `environment`);

create index `ix_clienttestwindow`
	on `client_testwindow`(`clientname`, `testid`);

create unique index `ix_clienttestmode`
	on `client_testmode`(`testkey`, `sessiontype`);

create index `ix_clienttimelimits`
	on `client_timelimits`(`clientname`, `_efk_testid`);

create index `ix_clienttooldependencies`
	on `client_tooldependencies`(`clientname`, `contexttype`, `context`);

create index `ix_clienttooltestid`
	on `client_testtool`(`context`);

create index `ix_coremsgcontext`
	on `tds_coremessageobject`(`context`);

create index `ix_form_testkey`
	on `client_testformproperties`(`testkey`);

create index `ix_geoclientapp`
	on `geo_clientapplication`(`clientname`, `environment`, `appname`, `servicetype`);

create index `ix_msgtrans`
	on `client_messagetranslation`(`_fk_coremessageobject`, `client`, `language`);

create index `ix_testeligibility`
	on `client_testeligibility`(`clientname`, `testid`, `enables`, `rtsname`, `rtsvalue`);

create index `ix_testmode`
	on `client_testmode`(`clientname`, `testid`, `sessiontype`);

create index `ix_testmodekey`
	on `client_testmode`(`testkey`);

create index `ix_testrtsspecs`
	on `client_testrtsspecs`(`clientname`, `testid`);

create index `ix_testgrade_test`
	on `client_testgrades`(`testid`);

create index `ix_testmode_test`
	on `client_testmode`(`testid`);

create index `ix_testprops_test`
	on `client_testproperties`(`testid`);

create index `ix_testwindow_test`
	on `client_testwindow`(`testid`);

create unique index `ix_uniqueappmsg`
	on `tds_coremessageobject`(`ownerapp`, `messageid`, `contexttype`, `context`);



alter table `client_fieldtestpriority`
	add constraint `fk_ft_testeeatt`
	foreign key(`clientname`, `tds_id`)
	references `client_testeeattribute`(`clientname`, `tds_id`)
	on delete cascade 
	on update cascade ;

alter table `client_messagetranslation`
	add constraint `fk_clientmsgtranslation`
	foreign key(`_fk_coremessageobject`)
	references `tds_coremessageobject`(`_key`)
	on delete cascade 
	on update no action ;

alter table `client_tds_rtsattributevalues`
	add constraint `fk_client_tds_rtsattributevalues_client_tds_rtsattribute`
	foreign key(`clientname`, `fieldname`, `_efk_entitytype`, `type`)
	references `client_tds_rtsattribute`(`clientname`, `fieldname`, `_efk_entitytype`, `type`)
	on delete no action 
	on update no action ;

alter table `client_testtool`
	add constraint `fk_clienttool_tooltype`
	foreign key(`clientname`, `context`, `contexttype`, `type`)
	references `client_testtooltype`(`clientname`, `context`, `contexttype`, `toolname`)
	on delete cascade 
	on update cascade ;

alter table `client_testwindow`
	add constraint `fk_timewindow`
	foreign key(`clientname`, `windowid`)
	references `client_timewindow`(`clientname`, `windowid`)
	on delete no action 
	on update cascade ;

alter table `geo_clientapplication`
	add constraint `fk_geoapp_db`
	foreign key(`_fk_geo_database`)
	references `geo_database`(`_key`)
	on delete no action 
	on update no action ;

alter table `system_applicationsettings`
	add constraint `tds_applicationsettings_client_applicationsettings`
	foreign key(`_fk_tds_applicationsettings`)
	references `tds_applicationsettings`(`_key`)
	on delete cascade 
	on update no action ;

alter table `tds_coremessageuser`
	add constraint `fk_msguser_msgobj`
	foreign key(`_fk_coremessageobject`)
	references `tds_coremessageobject`(`_key`)
	on delete no action 
	on update no action ;

alter table `tds_fieldtestpriority`
	add constraint `fk_tds_testeeatt`
	foreign key(`tds_id`)
	references `tds_testeeattribute`(`tds_id`)
	on delete no action 
	on update no action ;



/*
-- =============================================
-- create date: <create date,,>
-- description:	remove orphan tests config from other tables
-- =============================================
create trigger `testdelete` on  dbo.[client_testproperties]
   after delete
as 
begin
	set nocount on;

	delete from client_timelimits where exists 
		(select * from deleted where clientname=client_timelimits.clientname and testid=client_timelimits._efk_testid) default charset = UTF8;	
	
	delete from dbo.client_test_itemtypes where exists 
		(select * from deleted where clientname=client_test_itemtypes.clientname and testid=client_test_itemtypes.testid) default charset = UTF8;	

	delete from dbo.client_pilotschools where exists 
		(select * from deleted where clientname=client_pilotschools.clientname and testid=client_pilotschools._efk_testid) default charset = UTF8;	
	
	delete from dbo.client_testtool where exists 
		(select * from deleted where clientname=client_testtool.clientname and contexttype = 'test' and testid=client_testtool.context) default charset = UTF8;	
	
	delete from dbo.client_testtooltype where exists 
		(select * from deleted where clientname=client_testtooltype.clientname and contexttype = 'test' and testid=client_testtooltype.context) default charset = UTF8;
	
	delete from dbo.client_testscorefeatures where exists 
		(select * from deleted where clientname=client_testscorefeatures.clientname and testid=client_testscorefeatures.testid) default charset = UTF8;	

    delete from dbo.client_fieldtestpriority where exists 
		(select * from deleted where clientname=client_fieldtestpriority.clientname and testid=client_fieldtestpriority.testid) default charset = UTF8;	

    delete from dbo.client_segmentproperties where exists 
		(select * from deleted where clientname=client_segmentproperties.clientname and testid=client_segmentproperties.parenttest) default charset = UTF8;	

    delete from dbo.client_testwindow where exists 
		(select * from deleted where clientname=client_testwindow.clientname and testid=client_testwindow.testid) default charset = UTF8;	

    delete from dbo.client_testmode where exists
        (select * from deleted where clientname = client_testmode.clientname and testid = client_testmode.testid) default charset = UTF8;

    delete from dbo.client_testformproperties where exists
        (select * from deleted where clientname=client_testformproperties.clientname and testid=client_testformproperties.testid) default charset = UTF8;	

    delete from dbo.client_testgrades where exists
        (select * from deleted where clientname=client_testgrades.clientname and testid = client_testgrades.testid) default charset = UTF8;

end
;

*/