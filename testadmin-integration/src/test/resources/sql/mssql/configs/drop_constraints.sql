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
