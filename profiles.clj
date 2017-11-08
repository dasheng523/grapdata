;; WARNING
;; The profiles.clj file is used for local environment variables, such as database credentials.
;; This file is listed in .gitignore and will be excluded from version control by Git.

{:profiles/dev  {:env {:database-url "mysql://localhost:3306/grapdata_dev?user=db_user_name_here&password=db_user_password_here"}}
 :profiles/test {:env {:database-url "mysql://localhost:3306/grapdata_test?user=db_user_name_here&password=db_user_password_here"}}}
