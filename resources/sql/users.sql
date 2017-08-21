-- :name mysql-create-users-table :!
-- :doc Create users table
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `sex` smallint(5) DEFAULT NULL,
  `enable` smallint(5) DEFAULT NULL,
  `firstname` varchar(50) DEFAULT NULL,
  `lastname` varchar(50) DEFAULT NULL,
  `birthday` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `emailunique` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8

-- :name mysql-drop-users-table :!
-- :doc Drop users table if exists
drop table if exists users

-- :name mysql-get-all-users
-- :command :query
-- :result n
-- :doc Get all users info
select
--~ (if (seq (:cols params)) ":i*:cols" "*")
from users
where enable=1


-- :name mysql-update-by-id :! :n
/* :require [clojure.string :as string]
            [hugsql.parameters :refer [identifier-param-quote]] */
update :i:table set
/*~
(string/join ","
  (for [[field _] (:updates params)]
    (str (identifier-param-quote (name field) options)
        " = :v:updates." (name field))))
~*/
where id = :id


-- :name mysql-insert-table-data :i!
insert into :i:table
(:i*:cols)
values
(:v*:vals)



/*
下面是mail数据库的内容
*/

-- :name mysql-insert-mail-user :i!
INSERT INTO users (email, password)
VALUES (:email, ENCRYPT(:password));

-- :name mysql-create-domains-table :!
CREATE TABLE domains (domain varchar(50) NOT NULL, PRIMARY KEY (domain) );

-- :name mysql-create-forwardings-table :!
CREATE TABLE forwardings (source varchar(80) NOT NULL, destination TEXT NOT NULL, PRIMARY KEY (source) );

-- :name mysql-create-users-table :!
CREATE TABLE users (email varchar(80) NOT NULL, password varchar(20) NOT NULL, PRIMARY KEY (email) );

-- :name mysql-create-transport-table :!
CREATE TABLE transport ( domain varchar(128) NOT NULL default '', transport varchar(128) NOT NULL default '', UNIQUE KEY domain (domain) );
