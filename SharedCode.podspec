Pod::Spec.new do |spec|
    spec.name                     = 'SharedCode'
    spec.version                  = '0.1.8'
    spec.homepage                 = 'https://github.com/JetBrains/kotlin-native'
    spec.source                   = { :git => "https://github.com/Frerit/SharedCode.git", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
    spec.authors                  = 'Pragma S.A.'
    spec.license                  = ''
    spec.summary                  = 'Working from Kotlin/Native using CocoaPods'

    spec.static_framework         = true
    spec.vendored_frameworks      = "#{spec.name}/build/cocoapods/framework/#{spec.name}.framework"
    spec.libraries                = "c++"
    spec.module_name              = "#{spec.name}_umbrella"

    spec.pod_target_xcconfig = {
        'KOTLIN_TARGET[sdk=iphonesimulator*]' => 'ios_x64',
        'KOTLIN_TARGET[sdk=iphoneos*]' => 'ios_arm',
        'KOTLIN_TARGET[sdk=watchsimulator*]' => 'watchos_x86',
        'KOTLIN_TARGET[sdk=watchos*]' => 'watchos_arm',
        'KOTLIN_TARGET[sdk=appletvsimulator*]' => 'tvos_x64',
        'KOTLIN_TARGET[sdk=appletvos*]' => 'tvos_arm64',
        'KOTLIN_TARGET[sdk=macosx*]' => 'macos_x64'
    }

    spec.script_phases = [
        {
            :name => 'Build SharedCode',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "./gradlew" -p "$REPO_ROOT" :SharedCode:syncFramework \
                    -Pkotlin.native.cocoapods.target=$KOTLIN_TARGET \
                    -Pkotlin.native.cocoapods.configuration=$CONFIGURATION \
                    -Pkotlin.native.cocoapods.cflags="$OTHER_CFLAGS" \
                    -Pkotlin.native.cocoapods.paths.headers="$HEADER_SEARCH_PATHS" \
                    -Pkotlin.native.cocoapods.paths.frameworks="$FRAMEWORK_SEARCH_PATHS"
            SCRIPT
        }
    ]
end