DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`
(
    `id`       int(10) NOT NULL AUTO_INCREMENT,
    `username` varchar(64)  NOT NULL COMMENT '用户名',
    `password` varchar(128) NOT NULL COMMENT '密码',
    `sign`     varchar(255) DEFAULT NULL COMMENT '签名',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

insert into t_user(username, password, sign)
values ('边月', 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=', '');
insert into t_user(username, password, sign)
values ('侯梅希', 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=', '');