def BIN_CATALOG = ''
def ACC_PROPERTIES = ''
def ACC_BASE = ''
def ACC_USER = ''
def BSL_LS_PROPERTIES = ''
def CURRENT_CATALOG = ''
def TEMP_CATALOG = ''
def PROJECT_KEY = ''

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
                    commonMethods = load "./lib/CommonMethods.groovy"

                    BIN_CATALOG = "${sonar_catalog}/bin/"
                        
                    CURRENT_CATALOG = pwd()
                    TEMP_CATALOG = "${CURRENT_CATALOG}\\sonar_temp"
                    CURRENT_CATALOG = "${CURRENT_CATALOG}\\Repo"

                    // создаем/очищаем временный каталог
                    dir(TEMP_CATALOG) {
                        deleteDir()
                        writeFile file: 'acc.json', text: '{"issues": []}'
                        writeFile file: 'bsl-generic-json.json', text: '{"issues": []}'
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

                            if(env.PROJECT_NAME == null || env.PROJECT_NAME.isEmpty()) {
                                commonMethods.echoAndError("env.PROJECT_NAME is not setup in SetEnvironmentVars.groovy")
                            }
                            echo "PROJECT_NAME: ${env.PROJECT_NAME}"

                            // Настройки инструментов
                            if (fileExists("./Repo/${PROPERTIES_CATALOG}/acc.properties")) {
                                ACC_PROPERTIES = "./Repo/${PROPERTIES_CATALOG}/acc.properties"
                                echo "file exists: ${ACC_PROPERTIES}"
                            }

                            if (fileExists("./Repo/${PROPERTIES_CATALOG}/bsl-language-server.conf")) {
                                BSL_LS_PROPERTIES = "./Repo/${PROPERTIES_CATALOG}/bsl-language-server.conf"
                                echo "file exists: ${BSL_LS_PROPERTIES}"
                            }

                            SRC = "./${PROJECT_NAME}/src"
                            if (git_repo_branch == 'master') {
                                PROJECT_KEY = PROJECT_NAME
                            } else {
                                PROJECT_KEY = "${PROJECT_NAME}_${git_repo_branch}"
                            }
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

        stage('АПК') {
            steps {
                script {
                    Exception caughtException = null

                    try { timeout(time: env.TIMEOUT_FOR_ACC_STAGE.toInteger(), unit: 'MINUTES') {
                        def cmd_properties = "\"acc.propertiesPaths=${ACC_PROPERTIES};acc.catalog=${CURRENT_CATALOG};acc.sources=${SRC};"
                        cmd_properties = cmd_properties + "acc.result=${TEMP_CATALOG}\\acc.json;acc.projectKey=${PROJECT_KEY};acc.check=${ACC_check};"
                        cmd_properties = cmd_properties + "acc.recreateProject=${ACC_recreateProject}\""
                        
                        def ib_connection = "/S${env.ACC_BASE_SERVER1C}\\${env.ACC_BASE_NAME}"
                        
                        def command = "runner run --ibconnection ${ib_connection} --db-user ${ACC_USER} --command ${cmd_properties}"
                        command = command + " --execute \"${BIN_CATALOG}acc-export.epf\" --ordinaryapp=1"

                        returnCode = commonMethods.cmdReturnStatusCode(command)
    
                        echo "cmd status code $returnCode"
    
                        if (returnCode != 0) {
                            commonMethods.echoAndError("Error running ACC ${ACC_BASE} at ${server1c}")
                        }
                    }}
                        catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException excp) {
                            if (commonMethods.isTimeoutException(excp)) {
                                commonMethods.throwTimeoutException("${STAGE_NAME}")
                            }
                        }
                        catch (Throwable excp) {
                            echo "catched Throwable"
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