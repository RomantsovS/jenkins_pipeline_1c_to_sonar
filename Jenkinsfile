pipeline {

    parameters {
        string(defaultValue: "${env.PROJECT_NAME}", description: '* Имя проекта. Одинаковое для EDT, проекта в АПК и в сонаре. Обычно совпадает с именем конфигурации.', name: 'PROJECT_NAME')
        string(defaultValue: "${env.git_repo_url}", description: '* URL к гит-репозиторию, который необходимо проверить.', name: 'git_repo_url')
        string(defaultValue: "${env.git_repo_branch}", description: 'Ветка репозитория, которую необходимо проверить. По умолчанию master', name: 'git_repo_branch')
        string(defaultValue: "${env.sonar_catalog}", description: 'Каталог сонара, в котором лежит все, что нужно. По умолчанию C:/Sonar/', name: 'sonar_catalog')
        string(defaultValue: "${env.PROPERTIES_CATALOG}", description: 'Каталог с настройками acc.properties, bsl-language-server.conf и sonar-project.properties. По умолчанию ./Sonar', name: 'PROPERTIES_CATALOG')
        booleanParam(defaultValue: env.ACC_check== null ? true : env.ACC_check, description: 'Выполнять ли проверку АПК. Если нет, то будут получены существующие результаты. По умолчанию: true', name: 'ACC_check')
        booleanParam(defaultValue: env.ACC_recreateProject== null ? false : env.ACC_recreateProject, description: 'Пересоздать проект в АПК. Все данные о проекте будут собраны заново. По умолчанию: false', name: 'ACC_recreateProject')
        string(defaultValue: "${env.STEBI_SETTINGS}", description: 'Файл настроек для переопределения замечаний. Для файла из репо проекта должен начинатся с папки Repo, например .Repo/Sonar/settings.json. По умолчанию ./Sonar/settings.json', name: 'STEBI_SETTINGS')
        string(defaultValue: "${env.jenkinsAgent}", description: 'Нода дженкинса, на которой запускать пайплайн. По умолчанию master', name: 'jenkinsAgent')
        string(defaultValue: "${env.EDT_VERSION}", description: 'Используемая версия EDT. По умолчанию 1.13.0', name: 'EDT_VERSION')
        string(defaultValue: "${env.perf_catalog}", description: 'Путь к каталогу с замерами производительности, на основе которых будет рассчитано покрытие. Если пусто - покрытие не считается.', name: 'perf_catalog')
        string(defaultValue: "${env.git_credentials_Id}", description: 'ID Credentials для получения изменений из гит-репозитория', name: 'git_credentials_Id')
        string(defaultValue: "${env.rocket_channel}", description: 'Канал в рокет-чате для отправки уведомлений', name: 'rocket_channel')
    }
    agent {
        label "${(env.jenkinsAgent == null || env.jenkinsAgent == 'null') ? "master" : env.jenkinsAgent}"
    }
    options {
        timeout(time: 8, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage("Инициализация переменных") {
            steps {
                timestamps {
                    script {

                        rocket_channel = rocket_channel == null || rocket_channel == 'null' ? '' : rocket_channel

                        if (!rocket_channel.isEmpty() ) {
                            rocketSend channel: rocket_channel, message: "Sonar check started: [${env.JOB_NAME} ${env.BUILD_NUMBER}](${env.JOB_URL})", rawMessage: true
                        }
                        // Инициализация параметров значениями по умолчанию
                        sonar_catalog = sonar_catalog.isEmpty() ? "C:/Sonar/" : sonar_catalog
                        PROPERTIES_CATALOG = PROPERTIES_CATALOG.isEmpty() ? "./Sonar" : PROPERTIES_CATALOG
                        
                        EDT_VERSION = EDT_VERSION.isEmpty() ? '1.13.0' : EDT_VERSION
                        STEBI_SETTINGS = STEBI_SETTINGS.isEmpty() ? './Sonar/settings.json' : STEBI_SETTINGS
                        git_repo_branch = git_repo_branch.isEmpty() ? 'master' : git_repo_branch

                        perf_catalog = perf_catalog == null || perf_catalog == 'null' ? '' : perf_catalog
                        
                        BIN_CATALOG = "${sonar_catalog}/bin/"
                        ACC_BASE = "${sonar_catalog}/ACC/"
                        ACC_USER = 'Admin'
                        SRC = "./${PROJECT_NAME}/src"

                        // Подготовка переменных по переданным параметрам
                        // Настройки инструментов
                        ACC_PROPERTIES = "./Repo/${PROPERTIES_CATALOG}/acc.properties"
                        if (fileExists(ACC_PROPERTIES)) {
                            echo "file exists: ${ACC_PROPERTIES}"
                        } else {
                            echo "file does not exist: ${ACC_PROPERTIES}"
                            ACC_PROPERTIES = "./Sonar/acc.properties"
                        }
                        BSL_LS_PROPERTIES = "./Repo/${PROPERTIES_CATALOG}/bsl-language-server.conf"
                        if (fileExists(BSL_LS_PROPERTIES)) {
                            echo "file exists: ${BSL_LS_PROPERTIES}"
                        } else {
                            echo "file does not exist: ${BSL_LS_PROPERTIES}"
                            BSL_LS_PROPERTIES = "./Sonar/bsl-language-server.conf"
                        }
                        
                        CURRENT_CATALOG = pwd()
                        TEMP_CATALOG = "${CURRENT_CATALOG}\\sonar_temp"
                        EDT_VALIDATION_RESULT = "${TEMP_CATALOG}\\edt-result.csv"
                        CURRENT_CATALOG = "${CURRENT_CATALOG}\\Repo"

                        // создаем/очищаем временный каталог
                        dir(TEMP_CATALOG) {
                            deleteDir()
                            writeFile file: 'acc.json', text: '{"issues": []}'
                            writeFile file: 'bsl-generic-json.json', text: '{"issues": []}'
                            writeFile file: 'edt.json', text: '{"issues": []}'
                        }
                        PROJECT_NAME_EDT = "${CURRENT_CATALOG}\\${PROJECT_NAME}"
                        if (git_repo_branch == 'master') {
                            PROJECT_KEY = PROJECT_NAME
                        } else {
                            PROJECT_KEY = "${PROJECT_NAME}_${git_repo_branch}"
                        }
                        
                        GENERIC_ISSUE_JSON ="${TEMP_CATALOG}/acc.json,${TEMP_CATALOG}/bsl-generic-json.json,${TEMP_CATALOG}/edt.json"
                    }
                }
            }
        }
    }
}