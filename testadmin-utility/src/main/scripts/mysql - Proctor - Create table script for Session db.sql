create schema if not exists tdscore_dev_session2012_sandbox default charset = UTF8;

create table `adminevent`  ( 
	`_key`       	varbinary(16) not null,
	`eventtype`  	varchar(25) not null,
	`requestor`  	varchar(100) null,
	`description`	varchar(2000) null,
	`startdate`  	datetime(3) not null, -- constraint `df__admineven__start__457f2fde`  default (now()),
	`enddate`    	datetime(3) not null, -- constraint `df__admineven__endda__46735417`  default (date_add(now(), interval 4 hour)),
	`clientname` 	varchar(100) not null,
	constraint `pk_adminevent` primary key clustered(`_key`)
) default charset = UTF8;

create table `admineventitems`  ( 
	`_fk_adminevent` 	varbinary(16) not null,
	`_efk_itsbank`   	bigint not null,
	`_efk_itsitem`   	bigint not null,
	`alterationtype` 	varchar(50) not null,
	`alterationvalue`	varchar(25) null,
	`itemstart`      	datetime(3) null,
	`itemend`        	datetime(3) null 
) default charset = UTF8;

create table `admineventopportunities`  ( 
	`_fk_adminevent`     	varbinary(16) not null,
	`_fk_testopportunity`	varbinary(16) not null,
	`datecommitted`      	datetime(3) null,
	`reportingid`        	bigint null,
	`eventtype`          	varchar(25) null,
	`dateinserted`       	datetime(3), -- null constraint `df__admineven__datei__2a4129bd`  default (now()),
	`reason`             	nvarchar(255) null,
	constraint `pk_adminevntopps` primary key clustered(`_fk_adminevent`,`_fk_testopportunity`)
) default charset = UTF8;

create table `alertmessages`  ( 
	`_key`         	varbinary(16) not null,
	`title`        	varchar(50) not null,
	`message`      	varchar(500) not null,
	`datecreated`  	datetime(3) not null,
	`createduser`  	varchar(127) not null,
	`dateupdated`  	datetime(3) null,
	`updateduser`  	varchar(127) null,
	`datestarted`  	datetime(3) not null,
	`dateended`    	datetime(3) not null,
	`datecancelled`	datetime(3) null,
	`cancelleduser`	varchar(127) null,
	`clientname`   	varchar(100) not null,
	constraint `pk_alertmessages` primary key clustered(`_key`)
) default charset = UTF8;

create table `clientlatency`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`_fk_session`        	varbinary(16) null,
	`_fk_browser`        	varbinary(16) null,
	`page`               	int not null,
	`visitcount`         	int null,
	`visittime`          	float null,
	`createdate`         	datetime(3) null,
	`loaddate`           	datetime(3) null,
	`loadtime`           	float null,
	`requesttime`        	float null,
	`_date`              	datetime(3), -- not null constraint `df__clientlat___date__32d66fbe`  default (now()),
	`numitems`           	int null,
	`loadattempts`       	int null,
	`clientname`         	varchar(100) null,
	`contentsize`        	int null,
	`groupid`            	varchar(50) null,
	`siteid`             	varchar(100) null,
	`sitekey`            	bigint null,
	`sitename`           	varchar(200) null 
) default charset = UTF8;

create table `clientlatencyarchive`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`_fk_session`        	varbinary(16) null,
	`_fk_browser`        	varbinary(16) null,
	`page`               	int not null,
	`visitcount`         	int null,
	`visittime`          	int null,
	`createdate`         	datetime(3) null,
	`loaddate`           	datetime(3) null,
	`loadtime`           	int null,
	`requesttime`        	int null,
	`_date`              	datetime(3) not null,
	`numitems`           	int null,
	`loadattempts`       	int null,
	`clientname`         	varchar(100) null,
	`contentsize`        	float null,
	`groupid`            	varchar(50) null,
	`siteid`             	varchar(100) null,
	`sitekey`            	bigint null,
	`sitename`           	varchar(200) null 
) default charset = UTF8;

create table `client_os`  ( 
	`agentos`	varchar(200) not null,
	`os_id`  	varchar(25) null 
) default charset = UTF8;

create table `client_reportingid`  ( 
	`clientname`         	varchar(100) not null,
	`reportingid`        	bigint not null,
	`_fk_testopportunity`	varbinary(16) not null,
	`_date`              	datetime(3), -- not null constraint `df__client_re___date__5031c87b`  default (now()),
	constraint `pk_clientreportingid` primary key clustered(`clientname`,`reportingid`)
) default charset = UTF8;

create table `client_sessionid`  ( 
	`clientname`	varchar(100) not null,
	`idprefix`  	varchar(25) not null,
	`cnt`       	int not null,
	constraint `pk_clientsessionid` primary key clustered(`clientname`,`idprefix`)
) default charset = UTF8;

create table `ft_groupsamples`  ( 
	`_efk_adminsubject`	varchar(255) not null,
	`groupid`          	varchar(50) not null,
	`blockid`          	varchar(50) not null,
	`groupkey`         	varchar(100) not null,
	`samplesize`       	int not null,
	`_efk_parenttest`  	varchar(250) not null,
	`clientname`       	varchar(100) not null,
	`_fk_session`      	varbinary(16) null,
	`_date`            	datetime(3) -- null constraint `df__ft_groups___date__473e2675`  default (now()) 
	) default charset = UTF8;

create table `ft_opportunityitem`  ( 
	`_efk_fieldtest`      	varchar(200) not null,
	`position`            	int not null,
	`positionadministered`	int null,
	`numitems`            	int null,
	`_dategenerated`      	datetime(3), -- null constraint `df__ft_opport___date__3f073c79`  default (now()),
	`dateadministered`    	datetime(3) null,
	`groupkey`            	varchar(60) not null,
	`intervalsize`        	int null,
	`intervalstart`       	int null,
	`numintervals`        	int null,
	`itemsadministered`   	int null,
	`deleted`             	bit null,
	`_fk_testopportunity` 	varbinary(16) not null,
	`groupid`             	varchar(50) not null,
	`blockid`             	varchar(10) not null,
	`segment`             	int not null,
	`_efk_parenttest`     	varchar(250) null,
	`segmentid`           	varchar(100) null,
	`language`            	varchar(50) null,
	`dateassigned`        	datetime(3) null,
	`_fk_session`         	varbinary(16) null 
	) default charset = UTF8;

create table `geo_clientsystem`  ( 
	`url`           	varchar(256) null,
	`sessiondb`     	varchar(100) not null,
	`isactive`      	bit not null default 1,
	`clientname`    	varchar(100) not null,
	`_fk_geo_system`	varchar(32) not null,
	`systemrole`    	varchar(32) not null,
	`systemid`      	varchar(128) not null,
	constraint `pk_geoclientsystem` primary key clustered(`systemid`)
) default charset = UTF8;

create table `geo_session`  ( 
	`_fk_session`     	varbinary(16) not null,
	`_date`           	datetime(3) not null, -- constraint `df__geo_sessi___date__4b62f1bd`  default (now()),
	`isactive`        	bit not null,
	`logintoken`      	varbinary(16) not null,
	`_fk_clientsystem`	varchar(128) not null,
	`testeecount`     	int not null default 0
	) default charset = UTF8;

create table `itemdistribution`  ( 
	`clientname`       	varchar(100) not null,
	`_efk_adminsubject`	varchar(250) not null,
	`_efk_item`        	varchar(50) not null,
	`admincount`       	int not null default 0,
	`_fk_session`      	varbinary(16) null,
	`groupid`          	varchar(50) not null 
	) default charset = UTF8;

create table `loadtest_testee`  ( 
	`testeekey` 	bigint not null auto_increment,
	`sessionkey`	varbinary(16) not null,
    constraint `pk_loadtest_testee` primary key clustered(`testeekey`) 
	) default charset = UTF8;

create table `qc_validationexception`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`validationtype`     	varchar(50) not null,
	`xpath`              	varchar(150) null,
	`message`            	varchar(1000) null,
	`_efk_ruleid`        	varchar(50) null,
	`resultdocid`        	varchar(50) null,
	`dateentered`        	datetime(3) -- not null constraint `df__qc_valida__datee__59f03cdf`  default (now()) 
	) default charset = UTF8;

create table `rtsschoolgrades`  ( 
	`schoolkey`  	bigint not null,
	`grade`      	varchar(25) null,
	`dateentered`	datetime(3), -- not null constraint `df__rtsschool__datee__5e6ad939`  default (now()),
	`enrollment` 	int null,
	`clientname` 	varchar(100) not null 
	) default charset = UTF8;

create table `r_abnormallogins`  ( 
	`ipaddress`          	varchar(25) not null,
	`useragent`          	varchar(255) not null,
	`_date`              	datetime(3) not null,
	`_efk_testid`        	varchar(200) not null,
	`_fk_testopportunity`	varbinary(16) not null,
	`clientname`         	varchar(100) not null,
	`dategenerated`      	datetime(3) not null, -- constraint `df__r_abnorma__dateg__31783731`  default (now()),
	`districtid`         	varchar(100) null,
	`districtname`       	varchar(200) not null,
	`schoolid`           	varchar(100) null,
	`schoolname`         	varchar(200) not null,
	`testeeid`           	varchar(100) null 
	) default charset = UTF8;

create table `r_blueprintreport`  ( 
	`dategenerated`    	datetime(3), -- not null constraint `df__r_blueprintdate`  default (now()),
	`_efk_testid`      	varchar(150) not null,
	`strand`           	text not null,
	`_efk_strand`      	varchar(100) not null,
	`numtests`         	int not null,
	`minrequired`      	int null,
	`minadministered`  	int null,
	`maxrequired`      	int null,
	`maxadministered`  	int null,
	`meanadministered` 	float null,
	`_date`            	datetime(3) null,
	`clientname`       	varchar(100) not null,
	`_efk_adminsubject`	varchar(250) not null 
	) default charset = UTF8;

create table `r_geolatencyreport`  ( 
	`_date`        	datetime(3) not null,
	`districtname` 	varchar(100) null,
	`districtid`   	varchar(50) null,
	`schoolname`   	varchar(100) null,
	`schoolid`     	varchar(50) null,
	`clientn`      	int null,
	`clientmin`    	int null,
	`clientmax`    	int null,
	`clientmean`   	float null,
	`servermin`    	int null,
	`servermax`    	int null,
	`servermean`   	float null,
	`servern`      	int null,
	`dategenerated`	datetime(3), -- not null constraint `df__r_geolate__dateg__3c1fe2d6`  default (now()),
	`clientname`   	varchar(100) not null 
	) default charset = UTF8;

create table `r_hourlygeolatencytable`  ( 
	`_date`        	datetime(3) not null,
	`districtname` 	varchar(100) null,
	`districtid`   	varchar(50) null,
	`schoolname`   	varchar(100) null,
	`schoolid`     	varchar(50) null,
	`clientn`      	int null,
	`loadmin`      	int null,
	`loadmax`      	int null,
	`loadmean`     	float null,
	`requestmin`   	int null,
	`requestmax`   	int null,
	`requestmean`  	float null,
	`dategenerated`	datetime(3), -- not null constraint `df__r_hrgeolate__dateg__3c1fe2d6`  default (now()),
	`clientname`   	varchar(100) not null 
	) default charset = UTF8;

create table `r_hourlyusers`  ( 
	`_date`        	datetime(3) not null,
	`starttime`    	datetime(3) not null,
	`endtime`      	datetime(3) not null,
	`proctorcount` 	int null,
	`testeecount`  	int null,
	`label`        	varchar(100) null,
	`dategenerated`	datetime(3), -- not null constraint `df__r_hourlyu__dateg__6f9f86dc`  default (now()),
	`clientname`   	varchar(100) not null 
	) default charset = UTF8;

create table `r_participationcountstable`  ( 
	`_date`                	datetime(3) not null,
	`districtname`         	varchar(100) null,
	`districtid`           	varchar(50) null,
	`schoolname`           	varchar(100) null,
	`schoolid`             	varchar(50) null,
	`totalstudent`         	int null default 0,
	`totalstudentstarted`  	int null default 0,
	`totalstudentcompleted`	int null default 0,
	`dategenerated`        	datetime(3) not null, -- constraint `df__r_partici__dateg__20e1dcb5`  default (now()),
	`testid`               	varchar(150) null,
	`testname`             	varchar(255) null,
	`opportunity`          	int null,
	`clientname`           	varchar(100) not null 
	) default charset = UTF8;

create table `r_proctorpackage`  ( 
	`_key`       	bigint not null auto_increment,
	`proctorid`  	varchar(128) not null,
	`package`    	text not null,
	`iscurrent`  	bit null default 1,
	`datecreated`	datetime(3) null, -- constraint `df_r_proctorpackage_datecreated`  default (now()),
	`dateend`    	datetime(3) null,
	constraint `pk_r_proctorpackage` primary key clustered(`_key`)
) default charset = UTF8;

create table `r_schoolparticipationreport`  ( 
	`schoolrtskey`       	bigint not null,
	`lastname`           	varchar(100) null,
	`firstname`          	varchar(100) null,
	`ssid`               	varchar(100) null,
	`grade`              	varchar(10) null,
	`blocked`            	varchar(100) null,
	`lep`                	varchar(5) null,
	`_efk_testid`        	varchar(150) null,
	`accvalue`           	varchar(100) null,
	`opportunity`        	int null,
	`proctorname`        	varchar(100) null,
	`sessionid`          	varchar(100) null,
	`status`             	varchar(50) null,
	`reportingid`        	int null,
	`restart`            	int null,
	`graceperiodrestarts`	int null,
	`datestarted`        	varchar(20) null,
	`datecompleted`      	varchar(20) null,
	`lastactivity`       	varchar(20) null,
	`expiration`         	varchar(20) null,
	`_date`              	datetime(3) not null, -- constraint `df__r_schoolp___date__5792f321`  default (now()),
	`schoolid`           	varchar(100) null,
	`schoolname`         	varchar(200) null,
	`clientname`         	varchar(100) not null 
	) default charset = UTF8;

create table `r_studentpackage`  ( 
	`_key`       	bigint not null auto_increment,
	`ssid`       	varchar(20) not null,
	`stateid`    	char(2) not null,
	`package`    	text not null,
	`iscurrent`  	bit null default 1,
	`datecreated`	datetime(3) not null, -- constraint `df_r_studentpackage_datecreated`  default (now()),
	`dateend`    	datetime(3) null,
	constraint `pk_r_studentpackage` primary key clustered(`_key`)
) default charset = UTF8;

create table `r_testcounts`  ( 
	`_date`        	datetime(3) not null,
	`_efk_testid`  	varchar(150) null,
	`status`       	varchar(25) null,
	`count`        	int null,
	`dategenerated`	datetime(3) not null, -- constraint `df__r_testcou__dateg__6eab62a3`  default (now()),
	`clientname`   	varchar(100) not null 
	) default charset = UTF8;

create table `simp_itemgroup`  ( 
	`_fk_session` 	varbinary(16) not null,
	`_efk_segment`	varchar(250) not null,
	`groupid`     	varchar(50) not null,
	`maxitems`    	int not null,
	`passagekey`  	bigint null,
	constraint `pk_simpsegmentgroup` primary key clustered(`_fk_session`,`_efk_segment`,`groupid`)
) default charset = UTF8;

create table `simp_segment`  ( 
	`_fk_session`       	varbinary(16) not null,
	`_efk_adminsubject` 	varchar(255) not null,
	`_efk_segment`      	varchar(250) not null,
	`startability`      	float null,
	`startinfo`         	float null,
	`minitems`          	int null,
	`maxitems`          	int null,
	`ftstartpos`        	int null,
	`ftendpos`          	int null,
	`ftminitems`        	int null,
	`ftmaxitems`        	int null,
	`formselection`     	varchar(100) null,
	`blueprintweight`   	float not null,
	`cset1size`         	int not null,
	`cset2random`       	int not null,
	`cset2initialrandom`	int not null,
	`loadconfig`        	bigint null,
	`updateconfig`      	bigint null,
	`itemweight`        	float null,
	`abilityoffset`     	float null,
	`segmentposition`   	int null,
	`segmentid`         	varchar(150) null,
	`selectionalgorithm`	varchar(50) null,
	`cset1order`        	varchar(50) null,
	`blueprintkey`      	bigint null,
	`bankkey`           	bigint null,
	`its_name`          	varchar(150) null,
	constraint `pk_simptestsegment` primary key clustered(`_fk_session`,`_efk_segment`)
) default charset = UTF8;

create table `simp_segmentcontentlevel`  ( 
	`_fk_session` 	varbinary(16) not null,
	`_efk_segment`	varchar(250) not null,
	`contentlevel`	varchar(150) not null,
	`minitems`    	int not null,
	`maxitems`    	int not null,
	`adaptivecut` 	float null,
	`startability`	float null,
	`startinfo`   	float null,
	`scalar`      	float null,
	`isstrictmax` 	bit not null,
	`bpweight`    	float not null,
	`its_name`    	varchar(150) not null,
	`objecttype`  	varchar(100) null,
	constraint `pk_simpsegmentblueprint` primary key clustered(`_fk_session`,`_efk_segment`,`contentlevel`)
) default charset = UTF8;

create table `simp_segmentitem`  ( 
	`_fk_session` 	varbinary(16) not null,
	`_efk_segment`	varchar(250) not null,
	`_efk_item`   	varchar(50) not null,
	`isactive`    	bit not null,
	`isrequired`  	bit not null,
	`isfieldtest` 	bit not null,
	`strand`      	varchar(150) null,
	`groupid`     	varchar(50) null,
	`itemkey`     	bigint null,
	constraint `pk_simpsegmentitem` primary key clustered(`_fk_session`,`_efk_segment`,`_efk_item`)
) default charset = UTF8;

create table `simp_session`  ( 
	`_key`        	varbinary(16) not null, -- default uuid(),
	`_efk_proctor`	bigint null,
	`proctorid`   	varchar(128) null,
	`proctorname` 	varchar(128) null,
	`sessionid`   	varchar(128) null,
	`status`      	varchar(32) not null default 'closed',
	`name`        	varchar(255) not null default 'new session',
	`description` 	varchar(1024) null,
	`datecreated` 	datetime(3) not null, -- constraint `df_simpsession_datecreated`  default (now()),
	`clientname`  	varchar(100) not null,
	`language`    	varchar(50) null,
	constraint `pk_tblsimpsession` primary key clustered(`_key`)
) default charset = UTF8;

create table `simp_sessiontests`  ( 
	`_fk_session`      	varbinary(16) not null,
	`_efk_adminsubject`	varchar(255) not null,
	`_efk_testid`      	varchar(200) not null,
	`iterations`       	int null,
	`opportunities`    	int null,
	`meanproficiency`  	float null,
	`sdproficiency`    	float null,
	`strandcorrelation`	float null,
	constraint `pk_simpsessiontests` primary key clustered(`_fk_session`,`_efk_adminsubject`)
) default charset = UTF8;

create table `sim_itemgroup`  ( 
	`_fk_session` 	varbinary(16) not null,
	`_efk_segment`	varchar(250) not null,
	`groupid`     	varchar(50) not null,
	`maxitems`    	int not null,
	constraint `pk_segmentgroup` primary key clustered(`_fk_session`,`_efk_segment`,`groupid`)
) default charset = UTF8;

create table `sim_segment`  ( 
	`_fk_session`       	varbinary(16) not null,
	`_efk_adminsubject` 	varchar(255) not null,
	`_efk_segment`      	varchar(250) not null,
	`startability`      	float null,
	`startinfo`         	float null,
	`minitems`          	int null,
	`maxitems`          	int null,
	`ftstartpos`        	int null,
	`ftendpos`          	int null,
	`ftminitems`        	int null,
	`ftmaxitems`        	int null,
	`formselection`     	varchar(100) null,
	`blueprintweight`   	float not null,
	`cset1size`         	int not null,
	`cset2random`       	int not null,
	`cset2initialrandom`	int not null,
	`loadconfig`        	bigint null,
	`updateconfig`      	bigint null,
	`itemweight`        	float null,
	`abilityoffset`     	float null,
	`segmentposition`   	int null,
	`segmentid`         	varchar(150) null,
	`selectionalgorithm`	varchar(50) null,
	`cset1order`        	varchar(50) null,
	constraint `pk_sim_testsegment` primary key clustered(`_fk_session`,`_efk_segment`)
) default charset = UTF8;

create table `sim_segmentcontentlevel`  ( 
	`_fk_session` 	varbinary(16) not null,
	`_efk_segment`	varchar(250) not null,
	`contentlevel`	varchar(150) not null,
	`minitems`    	int not null,
	`maxitems`    	int not null,
	`adaptivecut` 	float null,
	`startability`	float null,
	`startinfo`   	float null,
	`scalar`      	float null,
	`isstrictmax` 	bit not null,
	`bpweight`    	float not null,
	constraint `pk_segmentblueprint` primary key clustered(`_fk_session`,`_efk_segment`,`contentlevel`)
) default charset = UTF8;

create table `sim_segmentitem`  ( 
	`_fk_session` 	varbinary(16) not null,
	`_efk_segment`	varchar(250) not null,
	`_efk_item`   	varchar(50) not null,
	`isactive`    	bit not null,
	`isrequired`  	bit not null,
	`isfieldtest` 	bit not null,
	`strand`      	varchar(150) null,
	`groupid`     	varchar(50) null,
	constraint `pk_sim_segmentitem` primary key clustered(`_fk_session`,`_efk_segment`,`_efk_item`)
) default charset = UTF8;

create table `sim_user`  ( 
	`userid`    	varchar(100) not null,
	`username`  	varchar(50) not null,
	`browserkey`	varbinary(16) not null, -- constraint `df__sim_user__browse__2260dc2a`  default (newid()),
	`password`  	varchar(25) null,
	`userkey`   	bigint not null default 0,
	constraint `pk_simuser` primary key clustered(`userid`)
) default charset = UTF8;

create table `sim_userclient`  ( 
	`_fk_simuser`	varchar(100) not null,
	`clientname` 	varchar(100) not null,
	`isadmin`    	bit not null default 0,
	constraint `pk_simuserclient` primary key clustered(`_fk_simuser`,`clientname`)
) default charset = UTF8;

create table `sirve_audit`  ( 
	`_date`           	datetime(3) not null, -- constraint `df__sirve_aud___date__30a40e89`  default (now()),
	`_fk_sirvesession`	varbinary(16) not null,
	`action`          	varchar(100) not null,
	`values`          	text null 
	) default charset = UTF8;

create table `sirve_session`  ( 
	`_key`         	varbinary(16) not null,
	`_dateaccessed`	datetime(3) null,
	`_efk_proctor` 	bigint null,
	`clientname`   	varchar(100) not null,
	`proctorid`    	varchar(25) not null,
	`proctorname`  	varchar(100) null,
	`status`       	varchar(25) null,
	constraint `pk_sirvesession` primary key clustered(`_key`)
) default charset = UTF8;

create table `session`  ( 
	`_key`            	varbinary(16) not null, -- constraint `df_tblsession__key`  default (newid()),
	`_efk_proctor`    	bigint null,
	`proctorid`       	varchar(128) null,
	`proctorname`     	varchar(128) null,
	`sessionid`       	varchar(128) null,
	`status`          	varchar(32) not null default 'closed',
	`name`            	varchar(255) not null default 'new session',
	`description`     	varchar(1024) null,
	`datecreated`     	datetime(3) not null, -- constraint `df_session_datecreated`  default (now()),
	`datebegin`       	datetime(3) null,
	`dateend`         	datetime(3) null,
	`serveraddress`   	varchar(255) null, -- constraint `df_session_serveraddress`  default (host_name()),
	`reserved`        	varchar(255) null,
	`datechanged`     	datetime(3) null,
	`datevisited`     	datetime(3) null,
	`clientname`      	varchar(100) not null,
	`_fk_browser`     	varbinary(16) not null,
	`environment`     	varchar(50) not null,
	`sessiontype`     	int not null default 0,
	`sim_language`    	varchar(50) null,
	`sim_proctordelay`	int not null default 2,
	`sim_abort`       	bit not null default 0,
	`sim_status`      	varchar(25) null,
	`sim_start`       	datetime(3) null,
	`sim_stop`        	datetime(3) null,
	constraint `pk_tblsession` primary key clustered(`_key`)
) default charset = UTF8;

create table `sessiontests`  ( 
	`_fk_session`      	varbinary(16) not null,
	`_efk_adminsubject`	varchar(255) not null,
	`_efk_testid`      	varchar(200) not null,
	`iterations`       	int null,
	`opportunities`    	int null,
	`meanproficiency`  	float null,
	`sdproficiency`    	float null,
	`strandcorrelation`	float null,
	`sim_threads`      	int null default 4,
	`sim_thinktime`    	int null default 0,
	constraint `pk_sessiontests` primary key clustered(`_fk_session`,`_efk_adminsubject`)
) default charset = UTF8;

create table `setofproctoralertmessages`  ( 
	`_efk_proctor`     	bigint not null,
	`_fk_alertmessages`	varbinary(16) not null,
	`datechanged`      	datetime(3) not null 
	) default charset = UTF8;

create table `testopportunity`  ( 
	`_efk_testee`         	bigint not null,
	`_efk_testid`         	varchar(255) not null,
	`opportunity`         	int not null default 1,
	`_fk_session`         	varbinary(16) null,
	`_fk_browser`         	varbinary(16) null,
	`testeeid`            	varchar(128) null,
	`testeename`          	varchar(128) null,
	`stage`               	varchar(50) null default 'new',
	`status`              	varchar(50) not null default 'pending',
	`prevstatus`          	varchar(50) null,
	`restart`             	int not null  default 0,
	`graceperiodrestarts` 	int null default 0,
	`datechanged`         	datetime(3) null, -- constraint `df_testopportunity_datechanged`  default (now()),
	`datejoined`          	datetime(3) null, -- constraint `df_testopportunity_datejoined`  default (now()),
	`datestarted`         	datetime(3) null,
	`daterestarted`       	datetime(3) null,
	`datecompleted`       	datetime(3) null,
	`datescored`          	datetime(3) null,
	`dateapproved`        	datetime(3) null,
	`dateexpired`         	datetime(3) null,
	`datesubmitted`       	datetime(3) null,
	`datereported`        	datetime(3) null,
	`comment`             	varchar(255) null,
	`abnormalstarts`      	int null default 0,
	`reportingid`         	bigint null,
	`xmlhost`             	varchar(50) null,
	`maxitems`            	int null default 0,
	`numitems`            	int null default 0,
	`dateinvalidated`     	datetime(3) null,
	`invalidatedby`       	varchar(100) null,
	`daterescored`        	datetime(3) null,
	`ft_archived`         	datetime(3) null,
	`items_archived`      	datetime(3) null,
	`subject`             	varchar(50) null,
	`datepaused`          	datetime(3) null,
	`expirefrom`          	datetime(3) null,
	`scoringdate`         	datetime(3) null,
	`scoremark`           	varbinary(16) null,
	`scorelatency`        	int null,
	`language`            	varchar(25) null,
	`proctorname`         	varchar(128) null,
	`sessid`              	varchar(128) null,
	`_key`                	varbinary(16) not null, -- constraint `df__testopport___key__18227982`  default (newid()),
	`clientname`          	varchar(100) not null,
	`datedeleted`         	datetime(3) null,
	`daterestored`        	datetime(3) null,
	`_version`            	int not null,
	`_efk_adminsubject`   	varchar(200) not null,
	`environment`         	varchar(50) not null,
	`_datewiped`          	datetime(3) null,
	`issegmented`         	bit not null,
	`algorithm`           	varchar(50) not null,
	`customaccommodations`	bit not null default 0,
	`numresponses`        	int not null default 0,
	`insegment`           	int null,
	`waitingforsegment`   	int null,
	`windowid`            	varchar(50) null,
	`dateforcecompleted`  	datetime(3) null,
	`dateexpiredreported` 	datetime(3) null,
	`mode`                	varchar(50) null default 'online',
	`itemgroupstring`     	text null,
	`initialability`      	float null,
	constraint `pk_testopportunity_guid` primary key clustered(`_key`)
) default charset = UTF8;

create table `testopportunitycontentcounts`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`_efk_testid`        	varchar(200) not null,
	`contentlevel`       	varchar(200) not null,
	`itemcount`          	int not null,
	`dateentered`        	datetime(3) not null, -- constraint `df__testoppor__datee__08ab2bc8`  default (now()),
	`_efk_adminsubject`  	varchar(250) not null,
	constraint `pk_testoppcontentcnts` primary key clustered(`_fk_testopportunity`,`contentlevel`)
) default charset = UTF8;

create table `testopportunityscores`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`measurelabel`       	varchar(100) not null,
	`value`              	varchar(100) null,
	`standarderror`      	float null,
	`measureof`          	varchar(150) not null,
	`isofficial`         	bit not null default 1,
	`_date`              	datetime(3) not null, -- constraint `df__testoppor___date__0e64051e`  default (now()),
	`subject`            	varchar(100) null,
	`useforability`      	bit not null default 0,
	`hostname`           	varchar(25) null, -- constraint `df__testoppor__hostn__104c4d90`  default (host_name()),
	constraint `pk_testopportunityscores` primary key clustered(`_fk_testopportunity`,`measureof`,`measurelabel`)
) default charset = UTF8;

create table `testopportunitysegment`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`_efk_segment`       	varchar(250) not null,
	`segmentposition`    	int not null,
	`formkey`            	varchar(50) null,
	`formid`             	varchar(200) null,
	`algorithm`          	varchar(50) null,
	`opitemcnt`          	int null,
	`ftitemcnt`          	int null,
	`ftitems`            	text null,
	`ispermeable`        	int not null default -1,
	`restorepermon`      	varchar(50) null,
	`segmentid`          	varchar(100) null,
	`entryapproved`      	datetime(3) null,
	`exitapproved`       	datetime(3) null,
	`formcohort`         	varchar(20) null,
	`issatisfied`        	bit not null default 0,
	`initialability`     	float null,
	`currentability`     	float null,
	`_date`              	datetime(3) not null, -- constraint `df__testoppor___date__48275db6`  default (now()),
	`dateexited`         	datetime(3) null,
	`itempool`           	text null,
	`poolcount`          	int null,
	constraint `pk_testoppsegment` primary key clustered(`_fk_testopportunity`,`segmentposition`)
) default charset = UTF8;

create table `testopportunitysegmentcounts`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`_efk_testid`        	varchar(200) not null,
	`contentlevel`       	varchar(200) not null,
	`itemcount`          	int not null,
	`dateentered`        	datetime(3) not null, -- constraint `df__testoppor__datee__681e60a5`  default (now()),
	`_efk_adminsubject`  	varchar(250) not null,
	`_efk_segment`       	varchar(250) not null,
	`minitems`           	int null,
	`maxitems`           	int null,
	constraint `pk_testoppsegmentcnts` primary key clustered(`_fk_testopportunity`,`_efk_segment`,`contentlevel`)
) default charset = UTF8;

create table `testopportunity_readonly`  ( 
	`_efk_testee`         	bigint null,
	`_efk_testid`         	varchar(255) null,
	`opportunity`         	int null,
	`_fk_session`         	varbinary(16) null,
	`_fk_browser`         	varbinary(16) null,
	`testeeid`            	varchar(128) null,
	`testeename`          	varchar(128) null,
	`stage`               	varchar(50) null,
	`status`              	varchar(50) null,
	`prevstatus`          	varchar(50) null,
	`restart`             	int null,
	`graceperiodrestarts` 	int null,
	`datechanged`         	datetime(3) null,
	`datejoined`          	datetime(3) null,
	`datestarted`         	datetime(3) null,
	`daterestarted`       	datetime(3) null,
	`datecompleted`       	datetime(3) null,
	`datescored`          	datetime(3) null,
	`dateapproved`        	datetime(3) null,
	`dateexpired`         	datetime(3) null,
	`datesubmitted`       	datetime(3) null,
	`datereported`        	datetime(3) null,
	`comment`             	varchar(255) null,
	`abnormalstarts`      	int null,
	`reportingid`         	bigint null,
	`odecreated`          	datetime(3) null,
	`odereported`         	datetime(3) null,
	`xmlhost`             	varchar(50) null,
	`maxitems`            	int null,
	`numitems`            	int null,
	`numresponses`        	int null,
	`dateinvalidated`     	datetime(3) null,
	`invalidatedby`       	varchar(100) null,
	`daterescored`        	datetime(3) null,
	`ft_archived`         	datetime(3) null,
	`items_archived`      	datetime(3) null,
	`subject`             	varchar(50) null,
	`datepaused`          	datetime(3) null,
	`accommodationstring` 	text null,
	`expirefrom`          	datetime(3) null,
	`customaccommodations`	bit null default 0,
	`language`            	varchar(25) null,
	`proctorname`         	varchar(128) null,
	`sessid`              	varchar(128) null,
	`_fk_testopportunity` 	varbinary(16) not null,
	`clientname`          	varchar(100) null,
	`_version`            	int null,
	`_efk_adminsubject`   	varchar(250) null,
	`insegment`           	int null,
	`waitingforsegment`   	int null,
	`windowid`            	varchar(50) null,
	`environment`         	varchar(50) null,
	`mode`                	varchar(50) null,
	constraint `pk_testopportunity_readonly` primary key clustered(`_fk_testopportunity`)
) default charset = UTF8;

create table `testeeaccommodations`  ( 
	`acctype`            	varchar(50) not null,
	`accvalue`           	varchar(250) not null,
	`acccode`            	varchar(250) not null,
	`_date`              	datetime(3) null, -- constraint `df__testeeacc___date__609d3a6e`  default (now()),
	`allowchange`        	bit null,
	`testeecontrol`      	bit null,
	`_fk_testopportunity`	varbinary(16) not null,
	`isapproved`         	bit not null default 0,
	`isselectable`       	bit not null default 0,
	`segment`            	int not null default 1,
	`valuecount`         	int null,
	`recordusage`        	bit not null default 0,
	constraint `pk_testeeaccommodations` primary key clustered(`_fk_testopportunity`,`acctype`,`acccode`,`segment`)
) default charset = UTF8;

create table `testeeattribute`  ( 
	`_fk_testopportunity`	varbinary(16) null,
	`tds_id`             	varchar(50) null,
	`attributevalue`     	varchar(400) null,
	`context`            	varchar(50) not null,
	`_date`              	datetime(3) not null -- constraint `df__testeeatt___date__7b113988`  default (now()) 
	) default charset = UTF8;

create table `testeecomment`  ( 
	`clientname`         	varchar(100) not null,
	`_efk_testee`        	bigint not null,
	`_fk_testopportunity`	varbinary(16) null,
	`itemposition`       	int null,
	`comment`            	text null,
	`_date`              	datetime(3) not null, -- constraint `df__testeecom___date__03d16812`  default (now()),
	`context`            	varchar(200) null,
	`_fk_session`        	varbinary(16) null,
	`groupid`            	varchar(50) null 
	) default charset = UTF8;

create table `testeehistory`  ( 
	`clientname`         	varchar(100) not null,
	`_efk_testee`        	bigint not null,
	`subject`            	varchar(100) not null,
	`initialability`     	float null,
	`opportunity`        	int null,
	`testid`             	varchar(200) null,
	`datechanged`        	datetime(3) null,
	`_efk_adminsubject`  	varchar(250) null,
	`_fk_testopportunity`	varbinary(16) null,
	`testedgrade`        	varchar(50) null,
	`testeeid`           	varchar(50) null,
	`_key`               	varbinary(16) not null, -- constraint `df__testeehist___key__5105f123`  default (newid()),
	`itemgroupstring`    	text null,
	constraint `pk_testeehistory` primary key clustered(`_key`)
) default charset = UTF8;

create table `testeeitemhistory`  ( 
	`clientname`         	varchar(100) not null,
	`_efk_testee`        	bigint not null,
	`dategenerated`      	datetime(3) null,
	`_fk_testopportunity`	varchar(64) not null,
	`groupid`            	varchar(50) not null,
	`asfieldtest`        	bit null,
	`deleted`            	bit not null default 0,
	`itempage`           	int null,
	`itemcount`          	int null,
	`_date`              	datetime(3) null -- constraint `df__testeeite___date__23d42350`  default (now()) 
	) default charset = UTF8;

create table `testeerelationship`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`tds_id`             	varchar(100) not null,
	`entitykey`          	bigint not null,
	`context`            	varchar(50) null,
	`_date`              	datetime(3) not null, -- constraint `df__testeerel___date__28d80438`  default (now()),
	`attributevalue`     	varchar(500) null,
	`relationship`       	varchar(100) null 
	) default charset = UTF8;

create table `testeeresponse`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`_efk_itsitem`       	bigint null,
	`_efk_itsbank`       	bigint null,
	`_fk_session`        	varbinary(16) null,
	`opportunityrestart` 	int null default 0,
	`page`               	int null,
	`position`           	int not null,
	`answer`             	varchar(10) null,
	`scorepoint`         	int null,
	`format`             	varchar(50) null,
	`isfieldtest`        	bit null default 0,
	`dategenerated`      	datetime(3) null,
	`datesubmitted`      	datetime(3) null,
	`datefirstresponse`  	datetime(3) null,
	`response`           	text null,
	`mark`               	bit not null default 0,
	`score`              	int not null default -1,
	`hostname`           	nchar(50) null,
	`numupdates`         	int null default 0,
	`datesystemaltered`  	datetime(3) null,
	`isinactive`         	bit not null default 0,
	`dateinactivated`    	datetime(3) null,
	`_fk_adminevent`     	varbinary(16) null,
	`groupid`            	varchar(50) null,
	`isselected`         	bit not null default 0,
	`isrequired`         	bit null default 0,
	`responsesequence`   	int not null default 0,
	`responselength`     	int null,
	`_fk_browser`        	varbinary(16) null,
	`isvalid`            	bit null default 0,
	`scorelatency`       	bigint null default 0,
	`groupitemsrequired` 	int not null default -1,
	`scorestatus`        	varchar(50) null,
	`scoringdate`        	datetime(3) null,
	`scoreddate`         	datetime(3) null,
	`scoremark`          	varbinary(16) null,
	`scorerationale`     	text null,
	`scoreattempts`      	int not null default 0,
	`_efk_itemkey`       	varchar(25) null,
	`_fk_responsesession`	varbinary(16) null,
	`segment`            	int null,
	`contentlevel`       	varchar(200) null,
	`segmentid`          	varchar(100) null,
	`groupb`             	float null,
	`itemb`              	float null,
	`datelastvisited`    	datetime(3) null,
	`visitcount`         	int not null default 0,
	`geosyncid`          	varbinary(16) null,
	`satellite`          	varchar(200) null, -- default (serverproperty('machinename')),
	constraint `pk_testoppresponse` primary key clustered(`_fk_testopportunity`,`position`)
) default charset = UTF8;

create table `testeeresponsearchive`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`_efk_itsitem`       	bigint not null,
	`_efk_itsbank`       	bigint not null,
	`_fk_session`        	varbinary(16) null,
	`opportunityrestart` 	int null,
	`page`               	int not null,
	`position`           	int not null,
	`answer`             	varchar(10) null,
	`scorepoint`         	int null,
	`format`             	varchar(50) null,
	`isfieldtest`        	bit null,
	`dategenerated`      	datetime(3) null,
	`datesubmitted`      	datetime(3) null,
	`datefirstresponse`  	datetime(3) null,
	`response`           	text null,
	`mark`               	bit not null,
	`score`              	int not null,
	`hostname`           	nchar(50) null,
	`numupdates`         	int null,
	`datesystemaltered`  	datetime(3) null,
	`isinactive`         	bit not null,
	`dateinactivated`    	datetime(3) null,
	`_fk_adminevent`     	varbinary(16) null,
	`groupid`            	varchar(50) null,
	`isselected`         	bit not null,
	`isrequired`         	bit null,
	`responsesequence`   	int not null,
	`responselength`     	int null,
	`_fk_browser`        	varbinary(16) null,
	`isvalid`            	bit null,
	`scorelatency`       	int null,
	`groupitemsrequired` 	int not null default -1,
	`scorestatus`        	varchar(50) null,
	`_auditkey`          	varbinary(16) null,
	`scoringdate`        	datetime(3) null,
	`scoreddate`         	datetime(3) null,
	`scorerationale`     	text null,
	`_efk_itemkey`       	varchar(25) not null,
	`_fk_responsesession`	varbinary(16) null,
	`segment`            	int not null,
	`contentlevel`       	varchar(200) null,
	`segmentid`          	varchar(100) null,
	`abilityestimate`    	float null,
	constraint `pk_testeeresponsearchive` primary key clustered(`_fk_testopportunity`,`position`,`responsesequence`)
) default charset = UTF8;

create table `testeeresponseaudit`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`sequence`           	int not null,
	`_date`              	datetime(3) not null, -- constraint `df__testeeres___date__6d2d2e85`  default (now()),
	`response`           	text not null,
	`sessionkey`         	varbinary(16) null,
	`browserkey`         	varbinary(16) null,
	`isselected`         	bit null,
	`isvalid`            	bit null,
	`scorelatency`       	int null,
	`score`              	int null,
	`scoringdate`        	datetime(3) null,
	`scoreddate`         	datetime(3) null,
	`scoremark`          	varbinary(16) null,
	`position`           	int not null,
	`_efk_item`          	varchar(50) null,
	`geosyncid`          	varbinary(16) null,
	`satellite`          	varchar(200) null -- constraint `df__testeeres__satel__66a0f7de`  default (serverproperty('machinename')) 
	) default charset = UTF8;

create table `testeeresponsescore`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`position`           	int not null,
	`scorestatus`        	varchar(50) not null,
	`scoringdate`        	datetime(3) null,
	`scoreddate`         	datetime(3) null,
	`scoremark`          	varbinary(16) null,
	`scoreattempts`      	int not null,
	`responsesequence`   	int not null,
	constraint `pk_responsescore` primary key clustered(`_fk_testopportunity`,`position`)
) default charset = UTF8;

create table `testoppabilityestimate`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`strand`             	varchar(100) not null,
	`estimate`           	float not null,
	`itempos`            	int not null,
	`info`               	float not null default 0.2,
	`lambda`             	float not null default 0.00632,
	`_date`              	datetime(3) not null -- constraint `df__testoppab___date__48076225`  default (now()) 
	) default charset = UTF8;

create table `testopprequest`  ( 
	`_key`               	varbinary(16) not null, -- constraint `df__testoppreq___key__6932806f`  default (newid()),
	`_fk_testopportunity`	varbinary(16) not null,
	`_fk_session`        	varbinary(16) not null,
	`requesttype`        	varchar(50) not null,
	`requestvalue`       	varchar(255) null,
	`datesubmitted`      	datetime(3) not null, -- constraint `df__testoppre__dates__6a26a4a8`  default (now()),
	`datefulfilled`      	datetime(3) null,
	`denied`             	varchar(250) null,
	`itempage`           	int null,
	`itemposition`       	int null,
	`requestparameters`  	varchar(255) null,
	`requestdescription` 	varchar(255) null,
	`datedenied`         	datetime(3) null,
	constraint `pk_testopprequest` primary key clustered(`_key`)
) default charset = UTF8;

create table `testopptoolsused`  ( 
	`_fk_testopportunity`	varbinary(16) not null,
	`itempage`           	int not null,
	`tooltype`           	varchar(100) not null,
	`toolcode`           	varchar(100) not null,
	`groupid`            	varchar(50) not null 
	) default charset = UTF8;

create table `_anonymoustestee`  ( 
	`_key`       	bigint not null auto_increment,
	`datecreated`	datetime(3) not null, -- constraint `df__anonymoustestee_datecreated`  default (now()),
	`clientname` 	varchar(100) not null,
	constraint `pk__anonymoustestee` primary key clustered(`_key`)
) default charset = UTF8;

create table `_externs`  ( 
	`clientname`      	varchar(100) not null,
	`environment`     	varchar(50) not null,
	`shiftwindowstart`	int not null default 0,
	`shiftwindowend`  	int not null default 0,
	`shiftformstart`  	int not null default 0,
	`shiftformend`    	int not null default 0,
	`shiftftstart`    	int not null default 0,
	`shiftftend`      	int not null default 0,
	constraint `pk__externs` primary key clustered(`clientname`)
) default charset = UTF8;

create table `_maxtestopps`  ( 
	`numopps`   	int not null,
	`_time`     	datetime(3) not null,
	`clientname`	varchar(100) not null 
	) default charset = UTF8;

create table `_missingmessages`  ( 
	`context`    	varchar(200) not null,
	`contexttype`	varchar(200) null,
	`appkey`     	varchar(255) not null,
	`message`    	text not null,
	`application`	varchar(200) null 
	) default charset = UTF8;

create table `_sb_errorlog`  ( 
	`id`                	varbinary(16) not null,
	`conversationhandle`	varbinary(16) null,
	`errorcode`         	int null,
	`errormessage`      	text null,
	`procname`          	varchar(100) null,
	`_date`             	datetime(3) not null, -- constraint `df___sb_error___date__7dee718a`  default (now()),
	constraint `pk_sb_errorlog_7cfa4d51` primary key clustered(`id`)
) default charset = UTF8;

create table `_sb_messagehandler`  ( 
	`messagetype`	varchar(200) not null,
	`handlerproc`	varchar(200) not null,
	constraint `pk_sb_messagehandl_7b1204df` primary key clustered(`messagetype`)
) default charset = UTF8;

create table `_sb_messages`  ( 
	`_key`              	varbinary(16) not null,
	`conversationhandle`	varbinary(16) not null,
	`conversationgroup` 	varbinary(16) null,
	`messagebody`       	text not null,
	`datesent`          	datetime(3) null,
	`datereceived`      	datetime(3) null,
	`dateended`         	datetime(3) null,
	`messagetype`       	varchar(200) not null,
	`dateprocessed`     	datetime(3) null,
	`handlerproc`       	varchar(100) null,
	`queueproc`         	varchar(100) null,
	`processingerror`   	text null,
	`dateresponded`     	datetime(3) null,
	constraint `pk_sb_messages_7fd6b9fc` primary key clustered(`_key`)
) default charset = UTF8;

create table `_sb_messagesarchive`  ( 
	`_key`              	varbinary(16) not null,
	`conversationhandle`	varbinary(16) not null,
	`conversationgroup` 	varbinary(16) null,
	`messagebody`       	text not null,
	`datesent`          	datetime(3) null,
	`datereceived`      	datetime(3) null,
	`dateended`         	datetime(3) null,
	`messagetype`       	varchar(200) not null,
	`dateprocessed`     	datetime(3) null,
	`queueproc`         	varchar(100) null,
	`handlerproc`       	varchar(100) null,
	`processingerror`   	text null,
	`_datearchived`     	datetime(3) not null, -- constraint `df___sb_messa___date__14d1d6e2`  default (now()),
	constraint `pk_sb_messagesarch_13ddb2a9` primary key clustered(`_key`)
) default charset = UTF8;

create table `_sitelatency`  ( 
	`n`             	int null,
	`_date`         	datetime(3) not null, -- constraint `df___schoolla___date__16252277`  default (now()),
	`bytespersecond`	float null,
	`clientname`    	varchar(100) not null,
	`enddate`       	datetime(3) null,
	`siteid`        	varchar(50) null,
	`sitekey`       	bigint not null,
	`sitename`      	varchar(200) null,
	`startdate`     	datetime(3) null,
	constraint `pk_schoollatency` primary key clustered(`clientname`,`sitekey`)
) default charset = UTF8;

create table `_synonyms`  ( 
	`dbname`	varchar(200) not null,
	`_date` 	datetime(3) not null, -- constraint `df_dbo__synonyms__date`  default (now()),
	`prefix`	varchar(200) not null,
	constraint `pk_synonyms` primary key clustered(`prefix`)
) default charset = UTF8;

create table `tblclsclientsessionstatus`  ( 
	`clsserversessionkey`  	bigint not null,
	`username`             	varchar(64) not null,
	`iislocalsessionid`    	varchar(64) null,
	`_efk_status`          	bigint not null,
	`lastactivitytimestamp`	datetime(3) not null,
	`clientkey`            	bigint not null,
	constraint `pk_tblclsclientsessionstatus` primary key clustered(`clsserversessionkey`,`clientkey`)
) default charset = UTF8;


/**********************************************Foreign key Constraints*****************************************/
alter table `admineventitems`
	add constraint `fk_admineventitems`
	foreign key(`_fk_adminevent`)
	references `adminevent`(`_key`)
	on delete cascade 
	on update no action ;

alter table `admineventopportunities`
	add constraint `fk_event_testopp`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `admineventopportunities`
	add constraint `fk_admineventopp`
	foreign key(`_fk_adminevent`)
	references `adminevent`(`_key`)
	on delete cascade 
	on update cascade ;

alter table `clientlatency`
	add constraint `fk_clientlatency`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `clientlatencyarchive`
	add constraint `fk_clientlatencyarchive`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `ft_opportunityitem`
	add constraint `fk_ftoppitem_oppkey`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `qc_validationexception`
	add constraint `fk_validation`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete no action 
	on update no action ;

alter table `simp_itemgroup`
	add constraint `fk_simpsegmentitemgroup`
	foreign key(`_fk_session`, `_efk_segment`)
	references `simp_segment`(`_fk_session`, `_efk_segment`)
	on delete cascade 
	on update no action ;

alter table `simp_segment`
	add constraint `fk_simpsegmenttest`
	foreign key(`_fk_session`, `_efk_adminsubject`)
	references `sessiontests`(`_fk_session`, `_efk_adminsubject`)
	on delete cascade 
	on update no action ;

alter table `simp_segment`
	add constraint `fk_simpsegementtest`
	foreign key(`_fk_session`, `_efk_adminsubject`)
	references `simp_sessiontests`(`_fk_session`, `_efk_adminsubject`)
	on delete cascade 
	on update no action ;

alter table `simp_segmentcontentlevel`
	add constraint `fk_simpsegmentcl`
	foreign key(`_fk_session`, `_efk_segment`)
	references `simp_segment`(`_fk_session`, `_efk_segment`)
	on delete cascade 
	on update no action ;

alter table `simp_segmentitem`
	add constraint `fk_simpsegmentitem`
	foreign key(`_fk_session`, `_efk_segment`)
	references `simp_segment`(`_fk_session`, `_efk_segment`)
	on delete cascade 
	on update no action ;

alter table `simp_session`
	add constraint `fk_session`
	foreign key(`_key`)
	references `session`(`_key`)
	on delete no action 
	on update no action ;

alter table `simp_sessiontests`
	add constraint `fk_simpsessiontests`
	foreign key(`_fk_session`)
	references `simp_session`(`_key`)
	on delete cascade 
	on update no action ;

alter table `sim_itemgroup`
	add constraint `fk_segmentitemgroup`
	foreign key(`_fk_session`, `_efk_segment`)
	references `sim_segment`(`_fk_session`, `_efk_segment`)
	on delete cascade 
	on update no action ;

alter table `sim_segment`
	add constraint `fk_simsegmenttest`
	foreign key(`_fk_session`, `_efk_adminsubject`)
	references `sessiontests`(`_fk_session`, `_efk_adminsubject`)
	on delete cascade 
	on update no action ;

alter table `sim_segmentcontentlevel`
	add constraint `fk_simsegmentcl`
	foreign key(`_fk_session`, `_efk_segment`)
	references `sim_segment`(`_fk_session`, `_efk_segment`)
	on delete cascade 
	on update no action ;

alter table `sim_segmentitem`
	add constraint `fk_simsegmentitem`
	foreign key(`_fk_session`, `_efk_segment`)
	references `sim_segment`(`_fk_session`, `_efk_segment`)
	on delete cascade 
	on update no action ;

alter table `sim_userclient`
	add constraint `fk_simclientuser`
	foreign key(`_fk_simuser`)
	references `sim_user`(`userid`)
	on delete cascade 
	on update cascade ;

alter table `sirve_audit`
	add constraint `fk_sirveaudit_session`
	foreign key(`_fk_sirvesession`)
	references `sirve_session`(`_key`)
	on delete cascade 
	on update no action ;

alter table `sessiontests`
	add constraint `fk_sessiontests`
	foreign key(`_fk_session`)
	references `session`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testopportunitycontentcounts`
	add constraint `fk_testoppcontentcnts`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testopportunityscores`
	add constraint `fk_testoppscores`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testopportunitysegment`
	add constraint `fk_testoppsegment`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testopportunitysegmentcounts`
	add constraint `fk_testoppsegmentcnts`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testeeaccommodations`
	add constraint `fk_testeeaccommodations`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testeeattribute`
	add constraint `fk_testeeatt_opp`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testeecomment`
	add constraint `fk_commenttestopp`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testeerelationship`
	add constraint `fk_testeerel_opp`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testeeresponse`
	add constraint `fk_testoppresponse`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete no action 
	on update no action ;

alter table `testeeresponsearchive`
	add constraint `fk_testoppresponsearchive`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete no action 
	on update no action ;

alter table `testeeresponseaudit`
	add constraint `fk_responseaudit_testop`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testeeresponsescore`
	add constraint `fk_responsescore_opp`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testoppabilityestimate`
	add constraint `fk_abilityest_testopp`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testopprequest`
	add constraint `fk_testopprequest`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;

alter table `testopptoolsused`
	add constraint `fk_opportunitytool`
	foreign key(`_fk_testopportunity`)
	references `testopportunity`(`_key`)
	on delete cascade 
	on update no action ;


/********************************** INDEXES ********************************************************/
create index `ix_abilityestimate`
	on `testoppabilityestimate`(`_fk_testopportunity`, `itempos`, `strand`);

create index `ix_accomoppcode`
	on `testeeaccommodations`(`_fk_testopportunity`, `acccode`, `acctype`);

create index `ix_admineventitem`
	on `admineventitems`(`_fk_adminevent`, `_efk_itsbank`, `_efk_itsitem`);

create index `ix_admineventopp_oppkey`
	on `admineventopportunities`(`_fk_testopportunity`);

create index `ix_clsuser`
	on `tblclsclientsessionstatus`(`username`);

create index `ix_clientalertmsgs`
	on `alertmessages`(`clientname`);

create index `ix_clientlatency_sitekey`
	on `clientlatencyarchive`(`sitekey`);

create index `ix_clientopplatency`
	on `clientlatency`(`_fk_testopportunity`, `page`);

create index `ix_clientopplatencyarchive`
	on `clientlatencyarchive`(`_fk_testopportunity`, `page`);

create index `ix_clientreportid_oppkey`
	on `client_reportingid`(`_fk_testopportunity`);

create unique index `ix_clientreportingid`
	on `testopportunity`(`clientname`, `reportingid`);

create index `ix_clienttestid`
	on `testopportunity`(`_efk_testid`, `clientname`, `status`);

create index `ix_datecompleted`
	on `testopportunity_readonly`(`datecompleted`, `_efk_testid`);

create index `ix_datereported`
	on `testopportunity_readonly`(`datereported`, `_efk_testid`);

create index `ix_datestarted`
	on `testopportunity_readonly`(`datestarted`, `_efk_testid`);

create index `ix_date_alertmessages`
	on `alertmessages`(`dateended`, `datecancelled`);

create index `ix_ftitem_cluster`
	on `ft_opportunityitem`(`_fk_testopportunity`, `groupid`);

create index `ix_ftoppitem_primary`
	on `ft_opportunityitem`(`_fk_testopportunity`, `segment`, `language`, `groupkey`);

create index `ix_ftsamples`
	on `ft_groupsamples`(`_efk_adminsubject`, `groupkey`);

create index `ix_ftsamples_parenttest`
	on `ft_groupsamples`(`_efk_parenttest`);

create index `ix_ft_oppitem_agg1`
	on `ft_opportunityitem`(`_efk_fieldtest`, `language`, `deleted`, `groupkey`)
	-- include (`_fk_testopportunity`)
;

create index `ix_geolatencyreportdistrict`
	on `r_geolatencyreport`(`districtid`, `_date`);

create index `ix_geolatencyreportschool`
	on `r_geolatencyreport`(`schoolid`, `_date`);

create index `ix_historyoppkey`
	on `testeehistory`(`_fk_testopportunity`);

create index `ix_historytesteeid`
	on `testeehistory`(`testeeid`);

create index `ix_hourlygeolatencydistrict`
	on `r_hourlygeolatencytable`(`districtid`, `_date`);

create index `ix_hourlygeolatencyschool`
	on `r_hourlygeolatencytable`(`schoolid`, `_date`);

create index `ix_itemhistoryoppgroup`
	on `testeeitemhistory`(`_fk_testopportunity`, `groupid`);

create index `ix_joined2`
	on `testopportunity`(`datejoined`);

create index `ix_joined2`
	on `testopportunity_readonly`(`datejoined`);

create index `ix_maxtestopps`
	on `_maxtestopps`(`_time`, `numopps`);

create index `ix_maxoppsclient`
	on `_maxtestopps`(`clientname`);

create index `ix_missingmessages`
	on `_missingmessages`(`application`, `context`, `contexttype`, `appkey`);

create index `ix_oppcontentcounts_testid`
	on `testopportunitycontentcounts`(`_efk_testid`, `contentlevel`);

create index `ix_oppdatestatus`
	on `testopportunity`(`datechanged`, `status`);

create index `ix_oppdatestatus`
	on `testopportunity_readonly`(`datechanged`, `status`);

create index `ix_participationcount`
	on `r_participationcountstable`(`_date`, `districtid`, `schoolid`, `testid`, `opportunity`);

create index `ix_participationreport`
	on `r_schoolparticipationreport`(`schoolrtskey`, `grade`, `_efk_testid`, `status`, `datecompleted`);

create index `ix_proctor_messages`
	on `setofproctoralertmessages`(`_efk_proctor`, `_fk_alertmessages`);

create index `ix_qcexcept_oppkey`
	on `qc_validationexception`(`_fk_testopportunity`);

create index `ix_rtestcounts`
	on `r_testcounts`(`_date`, `_efk_testid`, `status`);

create index `ix_r_proctorpackage_proctorid`
	on `r_proctorpackage`(`proctorid`);

create index `ix_r_studentpackage_ssid`
	on `r_studentpackage`(`ssid`);

create index `ix_reportabnormaldate`
	on `r_abnormallogins`(`_date`, `clientname`);

create index `ix_reportabnormaldistrict`
	on `r_abnormallogins`(`districtid`, `_date`, `clientname`);

create index `ix_reportabnormalschool`
	on `r_abnormallogins`(`schoolid`, `_date`, `clientname`);

create index `ix_responseitem`
	on `testeeresponse`(`_fk_testopportunity`, `_efk_itemkey`);

create index `ix_responsepage`
	on `testeeresponse`(`_fk_testopportunity`, `page`, `segment`);

create index `ix_responsescore`
	on `testeeresponsescore`(`scorestatus`, `scoringdate`)
	-- include (`_fk_testopportunity`, `position`, `responsesequence`, `scoreattempts`, `scoremark`)
;

create index `ix_sbmessages`
	on `_sb_messages`(`conversationhandle`);

create index `ix_sbmsgsarchivedate`
	on `_sb_messagesarchive`(`_datearchived`);

create index `ix_schoolkey`
	on `rtsschoolgrades`(`schoolkey`);

create unique index `ix_sessionid`
	on `session`(`sessionid`, `clientname`);

create index `ix_sessionproctor`
	on `session`(`_efk_proctor`, `clientname`);

create index `ix_syncresponseitem`
	on `testeeresponse`(`_fk_testopportunity`, `_efk_itemkey`, `geosyncid`, `position`);

create index `ix_testeeid`
	on `testopportunity_readonly`(`testeeid`);

create index `ix_testoppcontentlevel`
	on `testopportunitycontentcounts`(`contentlevel`);

create index `ix_testopportunity2`
	on `testopportunity`(`reportingid`)
	-- include (`_key`)
;

create index `ix_testopportunity2`
	on `testopportunity_readonly`(`reportingid`);

create index `ix_testeeatt_idval`
	on `testeeattribute`(`tds_id`, `attributevalue`);

create index `ix_testeeatt_idval2`
	on `testeeattribute`(`tds_id`, `context`, `attributevalue`);

create index `ix_testeeattribute_oppkey`
	on `testeeattribute`(`_fk_testopportunity`, `tds_id`);

create index `ix_testeecomment_oppkey`
	on `testeecomment`(`_fk_testopportunity`);

create index `ix_testeehistory`
	on `testeehistory`(`_efk_testee`, `clientname`, `subject`)
	-- include (`initialability`, `itemgroupstring`)
;

create index `ix_testeeitemhistory`
	on `testeeitemhistory`(`clientname`, `_efk_testee`, `_fk_testopportunity`, `groupid`);

create index `ix_testeerelationship_oppkey`
	on `testeerelationship`(`_fk_testopportunity`, `tds_id`);

create index `ix_testeeresponseaudit`
	on `testeeresponseaudit`(`_fk_testopportunity`, `position`, `sequence`);

create index `ix_testeesession2`
	on `testopportunity`(`_fk_session`, `datechanged`);

create index `ix_testeesession2`
	on `testopportunity_readonly`(`_fk_session`, `datechanged`);

create unique index `ix_testoppkey`
	on `testopportunity`(`_efk_testee`, `_efk_testid`, `opportunity`, `clientname`, `_version`)
	-- include (`_key`)
;

create index `ix_testopprequest`
	on `testopprequest`(`_fk_session`, `_fk_testopportunity`);

create index `ix_toolopp`
	on `testopptoolsused`(`_fk_testopportunity`, `itempage`, `tooltype`);

create index `itemdist_testitem`
	on `itemdistribution`(`_efk_adminsubject`, `_efk_item`, `_fk_session`);




/******************************************************Triggers*************************************************/
/*
-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `adminevent_default` $$

create trigger `adminevent_default`
   before insert
   on  `adminevent`
for each row
begin

	-- set `startdate` to current date when null
	if new.`startdate` is null then 
		set new.`startdate` = now(3) default charset = UTF8;
	end if;
	
	-- set `enddate` to 4 hrs from `startdate`  
	set new.`enddate` = date_add(new.`startdate`, interval 4 hour) default charset = UTF8;

end $$

delimiter ;	


-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `admineventopportunities_default` $$

create trigger `admineventopportunities_default`
   before insert
   on  `admineventopportunities`
for each row
begin

	-- set `dateinserted` to current date when null
	if new.`dateinserted` is null then 
		set new.`dateinserted` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;


-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `clientlatency_default` $$

create trigger `clientlatency_default`
   before insert
   on  `clientlatency`
for each row
begin

	-- set `_date` to current date when null
	if new.`_date` is null then 
		set new.`_date` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;


-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `client_reportingid_default` $$

create trigger `client_reportingid_default`
   before insert
   on  `client_reportingid`
for each row
begin

	-- set `_date` to current date when null
	if new.`_date` is null then 
		set new.`_date` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;


-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `ft_groupsamples_default` $$

create trigger `ft_groupsamples_default`
   before insert
   on  `ft_groupsamples`
for each row
begin

	-- set `_date` to current date when null
	if new.`_date` is null then 
		set new.`_date` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;


-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `ft_opportunityitem_default` $$

create trigger `ft_opportunityitem_default`
   before insert
   on  `ft_opportunityitem`
for each row
begin

	-- set `_dategenerated` to current date when null
	if new.`_dategenerated` is null then 
		set new.`_dategenerated` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;


-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `geo_session_default` $$

create trigger `geo_session_default`
   before insert
   on  `geo_session`
for each row
begin

	-- set `_date` to current date when null
	if new.`_date` is null then 
		set new.`_date` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;


-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `ft_opportunityitem_default` $$

create trigger `ft_opportunityitem_default`
   before insert
   on  `ft_opportunityitem`
for each row
begin

	-- set `_dategenerated` to current date when null
	if new.`_dategenerated` is null then 
		set new.`_dategenerated` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;

-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `ft_opportunityitem_default` $$

create trigger `ft_opportunityitem_default`
   before insert
   on  `ft_opportunityitem`
for each row
begin

	-- set `_dategenerated` to current date when null
	if new.`_dategenerated` is null then 
		set new.`_dategenerated` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;

-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `ft_opportunityitem_default` $$

create trigger `ft_opportunityitem_default`
   before insert
   on  `ft_opportunityitem`
for each row
begin

	-- set `_dategenerated` to current date when null
	if new.`_dategenerated` is null then 
		set new.`_dategenerated` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;

-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `ft_opportunityitem_default` $$

create trigger `ft_opportunityitem_default`
   before insert
   on  `ft_opportunityitem`
for each row
begin

	-- set `_dategenerated` to current date when null
	if new.`_dategenerated` is null then 
		set new.`_dategenerated` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;

-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `ft_opportunityitem_default` $$

create trigger `ft_opportunityitem_default`
   before insert
   on  `ft_opportunityitem`
for each row
begin

	-- set `_dategenerated` to current date when null
	if new.`_dategenerated` is null then 
		set new.`_dategenerated` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;

-- =============================================
-- description:	to assign default values to the specific columns when no values are provided
-- =============================================
delimiter $$

drop trigger if exists `ft_opportunityitem_default` $$

create trigger `ft_opportunityitem_default`
   before insert
   on  `ft_opportunityitem`
for each row
begin

	-- set `_dategenerated` to current date when null
	if new.`_dategenerated` is null then 
		set new.`_dategenerated` = now(3) default charset = UTF8;
	end if;

end $$

delimiter ;


-- =============================================
-- description:	set up an internal 'replication subscriber' so that proctor reads and participation reports
-- can be isolated from the student testing performance requirements
-- =============================================
delimiter $$

drop trigger if exists testoppdelete $$

create trigger testoppdelete
   after delete 
   on testopportunity
for each row
begin

    delete 
    from testopportunity_readonly
	where _fk_testopportunity = old._key;

end $$

delimiter ;

-- =============================================
-- description:	set up an internal 'replication subscriber' so that proctor reads and participation reports
-- can be isolated from the student testing performance requirements
-- =============================================
delimiter $$

drop trigger if exists `testoppupdate` $$

create trigger `testoppupdate`
   after update
   on `testopportunity`
for each row 
begin

    --  remove 'deleted' opportunities from the readonly table ??
    delete 
    from testopportunity_readonly
    where _fk_testopportunity = old._key 
        and old.datedeleted is not null;

    
    insert into testopportunity_readonly (_fk_testopportunity) 
    select new._key 
    from testopportunity
    where i.datedeleted is null and not exists (select * from testopportunity_readonly where _fk_testopportunity = i._key ) default charset = UTF8;

    update testopportunity_readonly
	set 
            _efk_testee     = i._efk_testee
            ,_efk_testid     = i._efk_testid
            ,opportunity    = i.opportunity
           ,[_fk_session] = i._fk_session
           ,[_fk_browser] = i._fk_browser
          ,testeeid         = i.testeeid
           ,testeename      = i.testeename
           ,[status]		= i.`status`
           
           ,[restart]		= i.restart
           ,[graceperiodrestarts]	= i.graceperiodrestarts
           ,[datechanged]	= i.datechanged
           ,[datejoined]	= i.datejoined
           ,[datestarted]	= i.datestarted
           ,[daterestarted]	= i.daterestarted
           ,[datecompleted]	= i.datecompleted
           ,[datescored]	= i.datescored
           ,[dateapproved]	= i.dateapproved
           ,[dateexpired]	= i.dateexpired
           ,[datesubmitted]	= i.datesubmitted
           ,[datereported]	= i.datereported
           ,[abnormalstarts]	= i.abnormalstarts
           ,[reportingid]	= i.reportingid
           ,subject             = i.subject
           
           ,[maxitems]			= i.maxitems
           ,[numitems]			= i.numitems
           ,numresponses        = i.numresponses
           ,[dateinvalidated]	= i.dateinvalidated
           ,[invalidatedby]		= i.invalidatedby
           ,[daterescored]		= i.daterescored

           ,datepaused          = i.datepaused
           ,_efk_adminsubject   = i._efk_adminsubject
           ,expirefrom          = i.expirefrom
           ,sessid              = i.sessid
           ,proctorname         = i.proctorname
           ,`language`          = i.`language`
           ,clientname          = i.clientname
           , _version           = i._version
           ,windowid              = i.windowid
           , mode               = i.mode
           ,customaccommodations = i.customaccommodations
           ,insegment           = i.insegment
           ,waitingforsegment   = i.waitingforsegment
           ,environment         = i.environment
	from inserted i
	where _fk_testopportunity = i._key;
end
;

-- =============================================
-- author:		albright
-- create date: 
-- description:	this trigger will populate the table testeeresponsescore with a reference to any item that is waiting for machine score.
-- it works in tandem with s_getscoreitems and s_updateitemscore.
-- the trigger is the most reliable method to ensure that item score status that are modified by any means are picked up.
-- =============================================
create trigger `updatemachinescore` 
   on  dbo.testeeresponse 
   after update
as 
begin
	-- set nocount on added to prevent extra result sets from
	-- interfering with select statements.

    update testeeresponsescore set scorestatus = i.scorestatus, scoringdate = i.scoringdate, scoreddate = null
		, scoremark = i.scoremark, scoreattempts = i.scoreattempts, responsesequence = i.responsesequence
	from inserted i
	where i.scorestatus = 'waitingformachinescore' and testeeresponsescore._fk_testopportunity = i._fk_testopportunity
		and testeeresponsescore.`position` = i.`position`;

    insert into testeeresponsescore (_fk_testopportunity, `position`, responsesequence, scorestatus, scoringdate, scoreddate, scoremark, scoreattempts)
	select _fk_testopportunity,`position`, responsesequence, scorestatus, scoringdate, scoreddate, scoremark, scoreattempts
	from inserted i 
	where i.scorestatus = 'waitingformachinescore'
	and not exists (select * from testeeresponsescore s where s._fk_testopportunity = i._fk_testopportunity and s.`position` = i.`position`);

end
;

*/