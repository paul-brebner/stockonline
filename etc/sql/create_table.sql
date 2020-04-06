drop table stockholding;
commit;

drop table stocktransaction;
commit;

drop table subaccount;
commit;

drop table stockitem;
commit;

create table subaccount
(sub_accno number(9) not null primary key,
 sub_name varchar(20),
 sub_address varchar(20),
 sub_credit number(9,2)
);

create table stockitem
(stock_id number(7) not null primary key,
 name varchar(10),
 code varchar(5) not null,
 current_val number(7,2) not null,
 high_val number(7,2),
 low_val number(7,2)
);

create table stocktransaction
(trans_id number(7) not null primary key,
 trans_type varchar(3) not null,
 sub_accno number(7) not null,
 stock_id number(7) not null,
 amount number(10) not null,
 price number(7,2) not null,
 trans_date varchar(12)
);

create table stockholding
(sub_accno number(7) not null,
 stock_id number(7) not null,
 amount number(10) not null,
 primary key (sub_accno, stock_id)
);
