-- :name create-source_article-table :!
-- :doc Create source_article table
CREATE TABLE `source_article` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `url` varchar(255) NOT NULL,
  `html` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


-- :name drop-source_article-table :!
-- :doc Drop source_article table if exists
drop table if exists `source_article`

-- :name insert-table-data :i!
insert into :i:table
(:i*:cols)
values
(:v*:vals)

-- :name insert-table-tuple :! :n
-- :doc Insert multiple characters with :tuple* parameter type
insert into :i:table
(:i*:cols)
values :tuple*:datas

-- :name select-all :? :*
-- :doc select all data from given table
select *
from :i:table

-- :name select-article-by-url :? :1
-- :doc select source_article by url
select *
from source_article
where `url` = :url

-- :name select-needfetch-by-url :? :1
-- :doc select source_article by url
select *
from need_fetch
where `url` = :url


-- :name pop-unvisit-url :!
delete from need_fetch
where `url` = :url

-- :name get-one-need_fetch :? :1
-- :doc get one limit amount record
select *
from need_fetch
limit 1

-- :name get-articel-by-id :? :1
-- :doc get-articel-by-id
select *
from source_article
where `id`=:id

-- :name get-all-needfetch :? :*
-- :doc get all needfetch data
select *
from need_fetch

-- :name get-all-urlhtml :? :*
-- :doc get all article data
select *
from source_article

-- :name get-all-article-html :? :*
-- :doc get all article html
select *
from source_article
WHERE html like '%entry-header overlay%';

-- :name get-article-by-url :? :1
-- :doc get article by url
select *
from article
where `url` = :url