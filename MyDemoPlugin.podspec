
  Pod::Spec.new do |s|
    s.name = 'MyDemoPlugin'
    s.version = '0.0.1'
    s.summary = 'Capacitor plugin'
    s.license = 'MIT'
    s.homepage = 'https://github.com/JoyZhou007/capacitorPlugin.git'
    s.author = 'joy'
    s.source = { :git => 'https://github.com/JoyZhou007/capacitorPlugin.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end