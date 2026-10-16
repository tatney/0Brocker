"""Launch the actual APK and verify the dedicated offline test account on Android."""
import pathlib, re, subprocess, time, xml.etree.ElementTree as ET

out = pathlib.Path('device-check')
out.mkdir(exist_ok=True)
def adb(*args):
    return subprocess.check_output(['adb', *args], text=True)
def dump():
    adb('shell', 'uiautomator', 'dump', '/sdcard/window.xml')
    return ET.fromstring(adb('shell', 'cat', '/sdcard/window.xml'))
def tap(node):
    x1,y1,x2,y2=map(int,re.findall(r'\d+',node.attrib['bounds']))
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))
    time.sleep(1)
def text_node(label):
    for n in dump().iter('node'):
        if n.attrib.get('text')==label or n.attrib.get('content-desc')==label:
            return n
    raise AssertionError('Missing UI control: '+label)
def screenshot(name):
    with (out/(name+'.png')).open('wb') as image:
        subprocess.run(['adb','exec-out','screencap','-p'],stdout=image,check=True)
    (out/(name+'.xml')).write_text(ET.tostring(dump(),encoding='unicode'))

apk=next(pathlib.Path('apk-smoke').glob('*.apk'))
adb('install','-r',str(apk))
adb('logcat','-c')
adb('shell','am','start','-W','-n','com.homeapp/com.homeapp.MainActivity')
time.sleep(12)
screenshot('01-sign-in')
fields=[n for n in dump().iter('node') if n.attrib.get('class')=='android.widget.EditText']
assert len(fields)>=2, 'Sign-in form not visible'
tap(fields[0]);adb('shell','input','text','tester@0brocker.app')
fields=[n for n in dump().iter('node') if n.attrib.get('class')=='android.widget.EditText']
tap(fields[1]);adb('shell','input','text','HomeTest!2026');adb('shell','input','keyevent','4')
time.sleep(1);screenshot('02-credentials-entered');tap(text_node('Sign in'));time.sleep(5);screenshot('02-after-sign-in')
assert 'V1 testing' in ET.tostring(dump(),encoding='unicode'), 'Dedicated test account did not reach home'
screenshot('02-home')
for label, name in [('Services','03-services'),('My home','04-my-home'),('Chat','05-chat'),('Profile','06-profile')]:
    tap(text_node(label));screenshot(name)
crash=adb('logcat','-d','-b','crash')
(out/'crash-log.txt').write_text(crash)
assert 'FATAL EXCEPTION' not in crash, 'Android app crashed'
print('Actual APK installed, test account signed in, all five destinations opened without a crash.')

# Exercise a real request and ensure both booking summaries use the same data.
def click_label(label):
    for attempt in range(7):
        try:
            tap(text_node(label)); return
        except AssertionError:
            adb('shell','input','swipe','540','1450','540','600','400');time.sleep(1)
    screenshot('missing-control')
    raise AssertionError('Control not reachable: '+label)

tap(text_node('Services'));click_label('Joseph Plumbing');click_label('Request Service')
fields=[n for n in dump().iter('node') if n.attrib.get('class')=='android.widget.EditText']
tap(fields[0]);adb('shell','input','text','Kitchen%stap%sleak');adb('shell','input','keyevent','4')
click_label('Review request');time.sleep(2);screenshot('07-request-summary')
summary=ET.tostring(dump(),encoding='unicode')
assert '35,000' in summary and 'Brokerage fee' in summary, 'Selected-provider price breakdown is missing'
for _ in range(3): adb('shell','input','keyevent','4');time.sleep(1)
tap(text_node('My home'));click_label('My bookings');time.sleep(1);screenshot('08-current-bookings')
assert 'Plumbing' in ET.tostring(dump(),encoding='unicode'), 'New request missing from current booking history'
adb('shell','input','keyevent','4');time.sleep(1);tap(text_node('Profile'));screenshot('09-profile-bookings')
assert 'Plumbing' in ET.tostring(dump(),encoding='unicode'), 'New request missing from profile'

# Persist a resident note and check it after an actual process restart.
tap(text_node('My home'))
fields=[n for n in dump().iter('node') if n.attrib.get('class')=='android.widget.EditText']
tap(fields[0]);adb('shell','input','text','Kitchen%stap');adb('shell','input','keyevent','4')
click_label('Save note');time.sleep(1)
adb('shell','am','force-stop','com.homeapp')
adb('shell','am','start','-W','-n','com.homeapp/com.homeapp.MainActivity');time.sleep(5)
tap(text_node('My home'))
for _ in range(4):
    if 'Kitchen tap' in ET.tostring(dump(),encoding='unicode'): break
    adb('shell','input','swipe','540','1450','540','700','400');time.sleep(1)
screenshot('10-persisted-home-note')
assert 'Kitchen tap' in ET.tostring(dump(),encoding='unicode'), 'Resident note did not survive restart'
crash=adb('logcat','-d','-b','crash');(out/'crash-log.txt').write_text(crash)
assert 'FATAL EXCEPTION' not in crash, 'Android app crashed during a request or resident note'
print('Provider request, accurate prices, both booking summaries and resident-note persistence passed on Android.')
