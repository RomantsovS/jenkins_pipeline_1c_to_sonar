def BIN_CATALOG = ''
def ACC_PROPERTIES = ''
def ACC_BASE = ''
def ACC_USER = ''
def BSL_LS_PROPERTIES = ''
def TEMP_CATALOG = ''
def PROJECT_KEY = ''
def JAVA_11_BIN = ''
def GENERIC_ISSUE_JSON = ''
def CURRENT_CATALOG = ''

pipeline {

    parameters {
        string(defaultValue: "${env.git_repo_url}", description: '* URL к гит-репозиторию, который необходимо проверить.', name: 'git_repo_url')
        booleanParam(defaultValue: env.ACC_stage == null ? true : env.ACC_stage, description: 'Выполнять ли шаг проверки АПК в целом. По умолчанию: true', name: 'ACC_stage')
        booleanParam(defaultValue: env.ACC_check == null ? true : env.ACC_check, description: 'Выполнять ли проверку АПК. Если нет, то будут получены существующие результаты. По умолчанию: true', name: 'ACC_check')
        booleanParam(defaultValue: env.ACC_recreateProject == null ? false : env.ACC_recreateProject, description: 'Пересоздать проект в АПК. Все данные о проекте будут собраны заново. По умолчанию: false', name: 'ACC_recreateProject')
        booleanParam(defaultValue: env.BSL_server_stage == null ? true : env.BSL_server_stage, description: 'Выполнять ли шаг проверки BSL-server в целом. По умолчанию: true', name: 'BSL_server_stage')
        booleanParam(defaultValue: env.Sonar_stage == null ? true : env.Sonar_stage, description: 'Выполнять ли шаг Sonar. По умолчанию: true', name: 'Sonar_stage')
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

                    BIN_CATALOG = "${env.sonar_catalog}/bin/"
                        
                    CURRENT_CATALOG = pwd()
                    TEMP_CATALOG = "${CURRENT_CATALOG}\\sonar_temp"

                    // создаем/очищаем временный каталог
                    dir(TEMP_CATALOG) {
                        deleteDir()
                        writeFile file: 'acc.json', text: '{"issues": []}'
                        writeFile file: 'bsl-generic-json.json', text: '{"issues": []}'
                    }
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
                            branches: [[name: "*/${env.git_repo_branch}"]],
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
                                env.ACC_PROPERTIES = "./Repo/${PROPERTIES_CATALOG}/acc.properties"
                                echo "file exists: ${env.ACC_PROPERTIES}"
                            }

                            if (fileExists("./Repo/${PROPERTIES_CATALOG}/bsl-language-server.conf")) {
                                env.BSL_LS_PROPERTIES = "./Repo/${PROPERTIES_CATALOG}/bsl-language-server.conf"
                                echo "file exists: ${env.BSL_LS_PROPERTIES}"
                            }

                            if (env.git_repo_branch == 'master') {
                                PROJECT_KEY = env.PROJECT_NAME
                            } else {
                                PROJECT_KEY = "${env.PROJECT_NAME}_${env.git_repo_branch}"
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

        stage('ACC') {
            when { expression {params.ACC_stage} }

            steps {
                script {
                    Exception caughtException = null

                    try { timeout(time: env.TIMEOUT_FOR_ACC_STAGE.toInteger(), unit: 'MINUTES') {
                        def cmd_properties = "\"acc.propertiesPaths=${env.ACC_PROPERTIES};acc.catalog=./;acc.sources=src;"
                        cmd_properties = cmd_properties + "acc.result=${TEMP_CATALOG}\\acc.json;acc.projectKey=${PROJECT_KEY};acc.check=${ACC_check};"
                        cmd_properties = cmd_properties + "acc.recreateProject=${ACC_recreateProject}\""
                        
                        def ib_connection = "/S${env.ACC_BASE_SERVER1C}\\${env.ACC_BASE_NAME}"
                        
                        def command = "runner run --ibconnection ${ib_connection} --db-user ${env.ACC_USER} --command ${cmd_properties}"
                        command = command + " --execute \"./acc-export.epf\" --ordinaryapp=1"

                        returnCode = commonMethods.cmdReturnStatusCode(command)
    
                        echo "cmd status code $returnCode"
    
                        if (returnCode != 0) {
                            commonMethods.echoAndError("Error running ACC ${ACC_BASE_NAME} at ${ACC_BASE_SERVER1C}")
                        }

                        if(GENERIC_ISSUE_JSON != '') {
                            GENERIC_ISSUE_JSON = GENERIC_ISSUE_JSON + ","
                        }

                        GENERIC_ISSUE_JSON = "${TEMP_CATALOG}/acc.json"                        
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

        stage('bsl-language-server') {
            when { expression {params.BSL_server_stage} }

            steps {
                script {
                    Exception caughtException = null

                    try { timeout(time: env.TIMEOUT_FOR_ACC_STAGE.toInteger(), unit: 'MINUTES') {
                        def command = "${env.JAVA_11_HOME}/bin/java -Xmx8g -jar ${BIN_CATALOG}bsl-language-server.jar -a -s \"${CURRENT_CATALOG}/Repo/src\" -r generic"
                        command = command + " -c \"${env.BSL_LS_PROPERTIES}\" -o \"${TEMP_CATALOG}\""

                        returnCode = commonMethods.cmdReturnStatusCode(command)
    
                        echo "cmd status code $returnCode"
    
                        if (returnCode != 0) {
                            commonMethods.echoAndError("Error running bsl-language-server ${BIN_CATALOG} at ${TEMP_CATALOG}")
                        }

                        if(GENERIC_ISSUE_JSON != '') {
                            GENERIC_ISSUE_JSON = GENERIC_ISSUE_JSON + ","
                        }

                        GENERIC_ISSUE_JSON = "${TEMP_CATALOG}/bsl-generic-json.json"
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

        stage('Sonar scanner') {
            when { expression {params.Sonar_stage} }

            steps {
                script {
                    Exception caughtException = null

                    try { timeout(time: env.TIMEOUT_FOR_ACC_STAGE.toInteger(), unit: 'MINUTES') {
                        dir('Repo') {
                            withSonarQubeEnv('Sonar') {
                                def scanner_properties = "-Dsonar.bsl.languageserver.enabled=false"

                                if(GENERIC_ISSUE_JSON != '') {
                                    scanner_properties = scanner_properties + " -Dsonar.externalIssuesReportPaths=${GENERIC_ISSUE_JSON}"
                                }

                                /*if (!perf_catalog.isEmpty()) {
                                    scanner_properties = "${scanner_properties} -Dsonar.coverageReportPaths=\"${TEMP_CATALOG}\\genericCoverage.xml\""
                                }*/
                                
                                def scannerHome = tool 'SonarQubeScanner';
                                def command = """
                                @set JAVA_HOME=${env.JAVA_11_HOME}\\
                                @set SONAR_SCANNER_OPTS=-Xmx6g
                                ${scannerHome}\\bin\\sonar-scanner ${scanner_properties}"""
                                
                                returnCode = commonMethods.cmdReturnStatusCode(command)
    
                                echo "cmd status code $returnCode"
    
                                if (returnCode != 0) {
                                    commonMethods.echoAndError("Error running Sonar scanner")
                                }
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
    }

    post {
        always {
            script {
                commonMethods.emailJobStatus ("")
            }
        }
    }
}