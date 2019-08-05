#!groovy
@Library('metaborg.jenkins.pipeline@develop') _

gradlePipeline(
  upstreamProjects: ['/metaborg/spoofax.gradle/develop', '/metaborg/resource/develop'],
  slack: true,
  slackChannel: "#pie"
)
