#!groovy
@Library('metaborg.jenkins.pipeline') _

gradlePipeline(
  slack: true,
  slackChannel: '#spoofax3-dev',
  gradleBuildTasks: "build",
  gradlePublishTasks: "publish"
)
