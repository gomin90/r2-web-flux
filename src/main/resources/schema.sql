-- salesforce.account definition

-- Drop table

-- DROP TABLE salesforce.account;

CREATE TABLE salesforce.account (
	personmailingcountry varchar(80) NULL,
	personlastcurequestdate timestamp NULL,
	jigsaw varchar(20) NULL,
	personmailingcity varchar(40) NULL,
	customerpriority__c varchar(255) NULL,
	lastname varchar(80) NULL,
	personmailinggeocodeaccuracy varchar(255) NULL,
	shippinglongitude float8 NULL,
	personmailinglatitude float8 NULL,
	tickersymbol varchar(20) NULL,
	personemailbounceddate timestamp NULL,
	shippingstate varchar(80) NULL,
	persontitle varchar(80) NULL,
	yearstarted varchar(4) NULL,
	personassistantphone varchar(40) NULL,
	numberofemployees int4 NULL,
	numberoflocations__c float8 NULL,
	parentid varchar(18) NULL,
	personothercity varchar(40) NULL,
	personcontactid varchar(18) NULL,
	recordtypeid varchar(18) NULL,
	shippingpostalcode varchar(20) NULL,
	billingcity varchar(40) NULL,
	persondepartment varchar(80) NULL,
	personemail varchar(80) NULL,
	personotherlatitude float8 NULL,
	site varchar(80) NULL,
	billinglatitude float8 NULL,
	accountsource varchar(255) NULL,
	shippingcountry varchar(80) NULL,
	lastvieweddate timestamp NULL,
	shippinggeocodeaccuracy varchar(255) NULL,
	operatinghoursid varchar(18) NULL,
	personindividualid varchar(18) NULL,
	dandbcompanyid varchar(18) NULL,
	"name" varchar(255) NULL,
	personleadsource varchar(255) NULL,
	sic varchar(20) NULL,
	tradestyle varchar(255) NULL,
	lastmodifieddate timestamp NULL,
	languages__pc varchar(100) NULL,
	phone varchar(40) NULL,
	masterrecordid varchar(18) NULL,
	ownerid varchar(18) NULL,
	personmailingpostalcode varchar(20) NULL,
	ispersonaccount bool NULL,
	ownership varchar(255) NULL,
	isdeleted bool NULL,
	systemmodstamp timestamp NULL,
	lastmodifiedbyid varchar(18) NULL,
	shippingstreet varchar(255) NULL,
	personotherpostalcode varchar(20) NULL,
	lastactivitydate date NULL,
	personotherphone varchar(40) NULL,
	billingpostalcode varchar(20) NULL,
	naicscode varchar(8) NULL,
	personassistantname varchar(40) NULL,
	personlastcuupdatedate timestamp NULL,
	billinglongitude float8 NULL,
	personotherlongitude float8 NULL,
	personotherstate varchar(80) NULL,
	createddate timestamp NULL,
	billingstate varchar(80) NULL,
	accountnumber varchar(40) NULL,
	personmobilephone varchar(40) NULL,
	level__pc varchar(255) NULL,
	jigsawcompanyid varchar(20) NULL,
	naicsdesc varchar(120) NULL,
	personbirthdate date NULL,
	salutation varchar(255) NULL,
	shippingcity varchar(40) NULL,
	personmailinglongitude float8 NULL,
	personmailingstreet varchar(255) NULL,
	personemailbouncedreason varchar(255) NULL,
	shippinglatitude float8 NULL,
	createdbyid varchar(18) NULL,
	"type" varchar(255) NULL,
	personhomephone varchar(40) NULL,
	website varchar(255) NULL,
	personmailingstate varchar(80) NULL,
	billingcountry varchar(80) NULL,
	firstname varchar(40) NULL,
	sla__c varchar(255) NULL,
	personothercountry varchar(80) NULL,
	cleanstatus varchar(255) NULL,
	personothergeocodeaccuracy varchar(255) NULL,
	description text NULL,
	billinggeocodeaccuracy varchar(255) NULL,
	annualrevenue float8 NULL,
	rating varchar(255) NULL,
	photourl varchar(255) NULL,
	lastreferenceddate timestamp NULL,
	upsellopportunity__c varchar(255) NULL,
	fax varchar(40) NULL,
	active__c varchar(255) NULL,
	slaexpirationdate__c date NULL,
	personotherstreet varchar(255) NULL,
	sicdesc varchar(80) NULL,
	industry varchar(255) NULL,
	billingstreet varchar(255) NULL,
	slaserialnumber__c varchar(10) NULL,
	dunsnumber varchar(9) NULL,
	sfid varchar(18) NULL COLLATE "ucs_basic",
	id serial4 NOT NULL,
	"_hc_lastop" varchar(32) NULL,
	"_hc_err" text NULL,
	CONSTRAINT account_pkey PRIMARY KEY (id)
);
CREATE INDEX hc_idx_account_lastmodifieddate ON salesforce.account USING btree (lastmodifieddate);
CREATE INDEX hc_idx_account_systemmodstamp ON salesforce.account USING btree (systemmodstamp);
CREATE UNIQUE INDEX hcu_idx_account_sfid ON salesforce.account USING btree (sfid);

-- Add state column to account table
ALTER TABLE salesforce.account 
ADD COLUMN IF NOT EXISTS state varchar(50);

-- Table Triggers

create trigger hc_account_logtrigger after
insert
    or
delete
    or
update
    on
    salesforce.account for each row
    when (((get_xmlbinary())::text = 'base64'::text)) execute function salesforce.hc_account_logger();
create trigger hc_account_status_trigger before
insert
    or
update
    on
    salesforce.account for each row execute function salesforce.hc_account_status();