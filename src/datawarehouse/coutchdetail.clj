(ns datawarehouse.coutchdetail
  (:require [com.ashafa.clutch :as clutch]))

(def html-db (clutch/get-database "html-db"))
(def domain-db (clutch/get-database "domain-db"))

;��������һ�����Դ���dbѡ��Ķ����������Է���һ�������ļ��ϡ��������д�����ˡ�
;������ֱ��������д����clutch�ĺ������������ܡ�


