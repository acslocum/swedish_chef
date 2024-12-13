require 'rubygems'
require 'uart'

class DeerAbby

  def initialize()
    @delimiter = ':'
    @usb_port = "/dev/cu.usbmodem14401"
    @speed = 115200
    @song_list = []
  end
  
  def respond
    next_thing = next_song
    puts next_thing
    speak(next_thing)
  end
  
  def speak(song)
    activate_chef
    puts "playing <#{song}> from #{@song_list}"
    `afplay "#{song}"`
  end
  
  def fill_song_list
    @song_list = []
    skits = Dir.entries("skits").select{|file| file.end_with?("mp4")}.shuffle!
    songs = Dir.entries("songs").select{|file| file.end_with?("mp4")}.shuffle!
    until(songs.empty?) 
      skit = "skits/#{skits.pop}"
      song = "songs/#{songs.pop}"
      @song_list << skit 
      @song_list << song
    end
    @song_list << "skits/#{skits.pop}"
    puts "created list #{@song_list}"
  end

  def next_song
    fill_song_list if @song_list.empty?
    puts "list #{@song_list}"
    song = @song_list.pop
    puts "song #{song}"
    return song
  end

  def activate_chef
    
    #thread = Thread.new { 
      UART.open @usb_port, @speed do |serial|
        25.times do  
          serial.puts(1) 
          puts "writing to serial"
          sleep 2
        end
      end
    #}
  end
  
end

puts "starting deer abby"
DeerAbby.new.activate_chef
#deer = DeerAbby.new
#while input = gets do
#  deer.activate_chef
  #deer.respond
#end