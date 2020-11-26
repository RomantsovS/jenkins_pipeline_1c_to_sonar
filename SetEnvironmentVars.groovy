env.VERBOSE = "true"
env.EMAIL_ADDRESS_FOR_NOTIFICATIONS = "romantsov_s@rusklimat.ru"

env.git_repo_branch = "master"
env.sonar_catalog = "C:/Sonar"
env.JAVA_11_HOME = sonar_catalog + "/jdk-11.0.2"
env.PROPERTIES_CATALOG = "./sonar"
env.STEBI_SETTINGS = ""

env.TIMEOUT_FOR_CHECKOUT_STAGE = "60"

//параметры для переопределения в одноименном файле в репозитории проекта в каталоге env.PROPERTIES_CATALOG/
env.TIMEOUT_FOR_ACC_STAGE = "60"
env.TIMEOUT_FOR_BSL_SERVER_STAGE = "60"
env.TIMEOUT_FOR_SONAR_STAGE = "60"

env.ACC_PROPERTIES = "./acc.properties"
env.BSL_LS_PROPERTIES = "./bsl-language-server.conf"

env.ACC_BASE_NAME = "AutoConfigurationCheck"
env.ACC_BASE_SERVER1C = "cv8app12:1741"
env.ACC_USER = "Administrator"