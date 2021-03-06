drop table PERFECTO_DEVICES;
drop table PERFECTO_DEVICE_CAPABILITIES;

create table PERFECTO_DEVICES
(
        NAME VARCHAR(50) NOT NULL,
        ID VARCHAR(100),
        MANUFACTURER VARCHAR(100) NOT NULL,
        MODEL VARCHAR(100) NOT NULL,
        OS VARCHAR(100),
        OS_VERSION VARCHAR(10),
        BROWSER_NAME VARCHAR(100) ,
        BROWSER_VERSION VARCHAR(10),
        ACTIVE VARCHAR(10) DEFAULT 'Y',
        AVAILABLE INTEGER DEFAULT 1,
        CONSTRAINT CHK_ACTIVE CHECK (ACTIVE IN ( 'Y', 'N' ))
);

create table PERFECTO_DEVICE_CAPABILITIES
(
        DEVICE_NAME VARCHAR(50) NOT NULL,
        NAME VARCHAR(50) NOT NULL,
        CLASS VARCHAR(50) DEFAULT 'STRING' NOT NULL,
        VALUE VARCHAR(1000) NOT NULL,
        CONSTRAINT CHK_CLASS CHECK (CLASS IN ( 'BOOLEAN', 'OBJECT', 'PLATFORM', 'STRING' ))
);


