#!groovy
@Library('metaborg.jenkins.pipeline@develop') _

gradlePipeline(
  upstreamProjects: [
    '/metaborg/spoofax.gradle/develop',
    '/metaborg/resource/develop',
    '/metaborg/spoofax-releng/master'
  ],
  slack: true,
  slackChannel: "#pie-dev"
)
