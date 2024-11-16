Pod::Spec.new do |spec|
    spec.name                     = 'HealthKMPSample'
    spec.version                  = '0.0.3'
    spec.homepage                 = 'https://github.com/vitoksmile/HealthKMP'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Wrapper for HealthKit on iOS and Google Fit and Health Connect on Android.'
    spec.vendored_frameworks      = 'build/cocoapods/framework/HealthKMPSample.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target    = '14.1'
    spec.dependency 'FirebaseAnalytics', '11.2.0'
    spec.dependency 'FirebaseAuth', '11.2.0'
    spec.dependency 'FirebaseCore', '11.2.0'
    spec.dependency 'FirebaseFirestore', '11.2.0'
    spec.dependency 'FirebaseRemoteConfig', '11.2.0'
                
    if !Dir.exist?('build/cocoapods/framework/HealthKMPSample.framework') || Dir.empty?('build/cocoapods/framework/HealthKMPSample.framework')
        raise "

        Kotlin framework 'HealthKMPSample' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :sample:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.xcconfig = {
        'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    }
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':sample',
        'PRODUCT_MODULE_NAME' => 'HealthKMPSample',
    }
                
    spec.script_phases = [
        {
            :name => 'Build HealthKMPSample',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
    spec.resources = ['build/compose/cocoapods/compose-resources']
end