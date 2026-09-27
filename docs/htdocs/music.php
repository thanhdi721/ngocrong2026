<audio id="bgm" autoplay>
  Trình duyệt của bạn không hỗ trợ audio.
</audio>

<script>
const playlist = ["/audio/ngauhung.mp3", "/audio/ainghe.mp3", "/audio/aldin.mp3"];
let current = parseInt(localStorage.getItem('audio_index')) || 0;
const audio = document.getElementById('bgm');

audio.src = playlist[current];
audio.volume = 0.5;

// Khôi phục thời gian
const lastTime = parseFloat(localStorage.getItem('audio_time'));
if (!isNaN(lastTime)) {
  audio.currentTime = lastTime;
}

audio.play().catch(() => {});

// Cập nhật trạng thái liên tục
setInterval(() => {
  localStorage.setItem('audio_index', current);
  localStorage.setItem('audio_time', audio.currentTime);
}, 1000);

audio.addEventListener('ended', () => {
  current = (current + 1) % playlist.length;
  audio.src = playlist[current];
  audio.play();
});
</script>
