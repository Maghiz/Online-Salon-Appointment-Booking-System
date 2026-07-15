// Theme Management
const themeToggle = document.getElementById('themeToggle');
const themeIcon = document.getElementById('themeIcon');
const html = document.documentElement;

if (themeToggle) {
    themeToggle.addEventListener('click', () => {
        const isDark = html.getAttribute('data-theme') === 'dark';
        const newTheme = isDark ? 'light' : 'dark';
        html.setAttribute('data-theme', newTheme);
        themeIcon.textContent = isDark ? '☀️' : '🌙';
        localStorage.setItem('salon-theme', newTheme);
    });
}

// Load saved theme
const savedTheme = localStorage.getItem('salon-theme');
if (savedTheme) {
    html.setAttribute('data-theme', savedTheme);
    if (themeIcon) themeIcon.textContent = savedTheme === 'dark' ? '🌙' : '☀️';
}

// Live Queue Polling
async function updateQueueStatus() {
    try {
        const response = await fetch("/api/queue");
        const data = await response.json();

        // Update Hero Mini Widgets
        const miniServing = document.getElementById('servingToken');
        const miniWait = document.getElementById('estWait');
        const miniCount = document.getElementById('queueCount');
        if (miniServing) miniServing.textContent = data.nowServing || 'NONE';
        if (miniWait) miniWait.textContent = (data.totalWaiting * 15) + ' mins';
        if (miniCount) miniCount.textContent = data.totalWaiting || '0';

        // Update Advanced Showcase (Now Serving)
        const lgToken = document.getElementById('servingTokenLarge');
        const lgGuest = document.getElementById('servingGuestName');
        const servingItem = data.queue ? data.queue.find(i => i.status === 'serving') : null;

        if (lgToken) lgToken.textContent = data.nowServing || 'NONE';
        if (lgGuest) lgGuest.textContent = servingItem ? servingItem.userName : 'Waiting for guests...';

        // Update Upcoming Queue Cards
        const cardsGrid = document.getElementById('liveQueueCards');
        if (cardsGrid) {
            const waitingList = data.queue ? data.queue.filter(i => i.status === 'waiting') : [];
            
            if (waitingList.length === 0) {
                cardsGrid.innerHTML = '<div class="queue-placeholder">Our artists are ready for you. Book now to join the queue!</div>';
            } else {
                cardsGrid.innerHTML = waitingList.map((item, index) => {
                    const estArrival = (index + 1) * 15;
                    const isNext = index === 0;
                    return `
                        <div class="wait-card glass-card ${isNext ? 'up-next' : ''} fade-in-up">
                            <div class="wait-token-box">${item.token}</div>
                            <div class="wait-details">
                                <h4>${item.userName}</h4>
                                <p>${isNext ? 'Ready Soon' : 'Preparing'}</p>
                                <div class="arrival-time">~${estArrival} mins</div>
                            </div>
                        </div>
                    `;
                }).join('');
            }
        }

        // Show AI Suggestion if wait is long
        if (data.totalWaiting > 5) {
            showAiSuggestion("Peak hours detected! Priority booking is available for VIP members.");
        } else {
            showAiSuggestion("Great time to visit! Minimal waiting for Haircut & Styling.");
        }
    } catch (error) {
        console.error("Queue fetch failed", error);
    }
}

// AI Suggestions
function showAiSuggestion(text) {
    const bubble = document.getElementById('aiSuggestion');
    const tip = document.getElementById('suggestionText');
    if (bubble && tip) {
        tip.textContent = text;
        bubble.style.display = 'block';
        setTimeout(() => bubble.style.display = 'none', 8000);
    }
}

// Chatbot Simulation
window.toggleChat = function () {
    const win = document.getElementById('chatWindow');
    win.style.display = win.style.display === 'none' ? 'block' : 'none';
}

window.handleChat = function (e) {
    if (e.key === 'Enter') {
        const input = document.getElementById('chatInput');
        const content = document.getElementById('chatContent');
        const msg = input.value.toLowerCase();

        content.innerHTML += `<p style="margin-bottom: 0.5rem; text-align: right;"><strong>You:</strong> ${input.value}</p>`;

        let reply = "I'm sorry, I didn't quite get that. Try asking about 'wait time' or 'services'.";
        if (msg.includes('wait') || msg.includes('time')) {
            reply = "Currently, the estimated wait is about 15-30 minutes. Booking online saves you a spot!";
        } else if (msg.includes('service') || msg.includes('price')) {
            reply = "We offer Haircuts starting at ₹800, Facials at ₹1200, and more. Check our Services page!";
        } else if (msg.includes('book')) {
            reply = "You can book directly from the 'Book Now' button in the menu!";
        }

        setTimeout(() => {
            content.innerHTML += `<p style="margin-bottom: 0.5rem; color: var(--primary);"><strong>AI:</strong> ${reply}</p>`;
            content.scrollTop = content.scrollHeight;
        }, 600);

        input.value = '';
    }
}

// Initial Calls
if (document.getElementById('servingToken')) {
    updateQueueStatus();
    setInterval(updateQueueStatus, 10000); // Poll every 10s
}
