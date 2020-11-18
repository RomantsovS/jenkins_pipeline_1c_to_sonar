def BIN_CATALOG = ''
def ACC_PROPERTIES = ''
def ACC_BASE = ''
def ACC_USER = ''
def BSL_LS_PROPERTIES = ''
def CURRENT_CATALOG = ''
def TEMP_CATALOG = ''
def PROJECT_KEY

pipeline {

    parameters {
        string(defaultValue: "${env.git_repo_url}", description: '* URL к гит-репозиторию, который необходимо проверить.', name: 'git_repo_url')
        booleanParam(defaultValue: env.ACC_check== null ? true : env.ACC_check, description: 'Выполнять ли проверку АПК. Если нет, то будут получены существующие результаты. По умолчанию: true', name: 'ACC_check')
        booleanParam(defaultValue: env.ACC_recreateProject== null ? false : env.ACC_recreateProject, description: 'Пересоздать проект в АПК. Все данные о проекте будут собраны заново. По умолчанию: false', name: 'ACC_recreateProject')
        string(defaultValue: "${env.jenkinsAgent}", description: 'Нода дженкинса, на которой запускать пайплайн. По умолчанию master', name: 'jenkinsAgent')
    }
    agent {
        label "${(env.jenkinsAgent == null || env.jenkinsAgent == 'null') ? "master" : env.jenkinsAgent}"
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '7'))
        timestamps()
        timeout(time: 8, unit: 'HOURS')
    }
    stages {
        stage("Инициализация переменных") {
            steps {
                script {
                    load "./SetEnvironmentVars.groovy"

                    BIN_CATALOG = "${sonar_catalog}/bin/"
                    ACC_BASE = "${sonar_catalog}/ACC/"
                    ACC_USER = 'Admin'
                    SRC = "./${PROJECT_NAME}/src"

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
                    CURRENT_CATALOG = "${CURRENT_CATALOG}\\Repo"

                    // создаем/очищаем временный каталог
                    dir(TEMP_CATALOG) {
                        deleteDir()
                        writeFile file: 'acc.json', text: '{"issues": []}'
                        writeFile file: 'bsl-generic-json.json', text: '{"issues": []}'
                        writeFile file: 'edt.json', text: '{"issues": []}'
                    }

                    if (git_repo_branch == 'master') {
                        PROJECT_KEY = PROJECT_NAME
                    } else {
                        PROJECT_KEY = "${PROJECT_NAME}_${git_repo_branch}"
                    }
                        GENERIC_ISSUE_JSON ="${TEMP_CATALOG}/acc.json,${TEMP_CATALOG}/bsl-generic-json.json,${TEMP_CATALOG}/edt.json"
                    }
                }
        }

        stage('Checkout') {
            steps {
                script {
                    Exception caughtException = null

                    try { timeout(time: env.TIMEOUT_FOR_CHECKOUT_STAGE.toInteger(), unit: 'MINUTES') {
                        dir('Repo') {
                            checkout([$class: 'GitSCM',
                            branches: [[name: "*/${git_repo_branch}"]],
                            doGenerateSubmoduleConfigurations: false,
                            extensions: [[$class: 'CheckoutOption', timeout: 60], [$class: 'CloneOption', depth: 0, noTags: true, reference: '', shallow: false,
                            timeout: 60]], submoduleCfg: [],
                            userRemoteConfigs: [[/*credentialsId: git_credentials_Id,*/ url: git_repo_url]]])

                            load "./${PROPERTIES_CATALOG}/SetEnvironmentVars.groovy"

                            echo "PROJECT_NAME: ${env.PROJECT_NAME}"
                        }
                    }}
                    catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException excp) {
                        if (commonMethods.isTimeoutException(excp)) {
                            commonMethods.throwTimeoutException("${STAGE_NAME}")
                        }
                    }
                    catch (Throwable excp) {
                        caughtException = excp
                    }

                    if (caughtException) {
                        error caughtException.message
                    }
                }
            }
        }
    }
}