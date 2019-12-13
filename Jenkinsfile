#!groovy
@Library('metaborg.jenkins.pipeline@develop') _

gradlePipeline(
  upstreamProjects: [
    '/metaborg/spoofax-releng/master'
  ],
  slack: true,
  slackChannel: "#pie-dev"
)
