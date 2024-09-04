const chatArea = document.getElementById('chatArea');
const userInput = document.getElementById('userInput');
const imageInput = document.getElementById('imageInput');
const sendButton = document.getElementById('sendButton');
const imageShow = document.getElementById('imageShow');
const chatRecordsSummaryArea = document.getElementById('chatRecordsSummaryArea');

const chatId = 1; // 替换为需要查询的聊天 ID

function onRecordSummaryItemSelected() {
    this.className = this.className + ' record-active';
}

function renderResponseWaiting() {
    const msgDiv = document.createElement('div');
    msgDiv.className = "assistant-message";
    msgDiv.id = 'message-loader'
    chatArea.appendChild(msgDiv);

    const headDiv = document.createElement('div');
    headDiv.className = 'head-icon';
    headDiv.textContent = "AI：";
    msgDiv.appendChild(headDiv);

    const contentDiv = document.createElement('div');
    contentDiv.className = 'content';
    contentDiv.style.display = 'flex';
    contentDiv.style.justifyContent = 'center';
    contentDiv.style.alignItems = 'center';
    msgDiv.appendChild(contentDiv);

    const loaderDiv = document.createElement('div');
    loaderDiv.className = 'mini-loader';
    contentDiv.appendChild(loaderDiv);
}

function renderInitLoading(parentDom) {
    const contentDiv = document.createElement('div');
    contentDiv.className = 'init-loader content';
    contentDiv.style.display = 'flex';
    contentDiv.style.height = '100%';
    contentDiv.style.justifyContent = 'center';
    contentDiv.style.alignItems = 'center';
    parentDom.appendChild(contentDiv);

    const loaderDiv = document.createElement('div');
    loaderDiv.className = 'loader';
    contentDiv.appendChild(loaderDiv);
}

function removeResponseWaiting() {
    document.getElementById('message-loader').remove();
}

function removeInitLoading(parentDom) {
    parentDom.querySelector('.init-loader').remove();
}

function renderChatRecordsSummary(records) {
    if(records == null || records.length === 0) {
        return;
    }

    records.forEach(record => {
        const rsItemDiv = document.createElement('div');
        rsItemDiv.className = "record-summary-item";
        rsItemDiv.setAttribute("record-id", record.chatId);
        rsItemDiv.addEventListener('click', onRecordSummaryItemSelected);

        const rsItemtTitleDiv = document.createElement('div');
        rsItemtTitleDiv.className = "record-summary-item-title";
        rsItemtTitleDiv.textContent = record.chatSummary;
        rsItemDiv.appendChild(rsItemtTitleDiv);

        chatRecordsSummaryArea.appendChild(rsItemDiv);
    })
}

// 获取聊天回话列表
async function fetchChatRecordsSummary() {
    renderInitLoading(chatRecordsSummaryArea);

    try {
        const response = await fetch('/chat_records_summary');
        const data = await response.json();
        removeInitLoading(chatRecordsSummaryArea);
        renderChatRecordsSummary(data);
    } catch (error) {
        console.error('Error fetching chat records summary:', error);
    }
}

// 获取聊天记录
async function fetchChatDetailHistory() {
    renderInitLoading(chatArea);

    try {
        const response = await fetch(`/chat/${chatId}`);
        const data = await response.json();
        removeInitLoading(chatArea);
        renderChatDetailHistory(data);
    } catch (error) {
        console.error('Error fetching chat history:', error);
    }
}

function renderChatRecordItem(record) {
    const msgDiv = document.createElement('div');
    msgDiv.className = record.role === "user" ? "user-message" : "assistant-message";
    chatArea.appendChild(msgDiv);

    const headDiv = document.createElement('div');
    headDiv.className = 'head-icon';
    headDiv.textContent = record.role === "user" ? "我：" : "AI：";
    msgDiv.appendChild(headDiv);

    const contentDiv = document.createElement('div');
    contentDiv.className = 'content';
    msgDiv.appendChild(contentDiv);

    record.contents.forEach(content => {
        if(content.type === "TEXT") {
            contentDiv.textContent = content.text; // 假设record有userInput字段
        }

        // 若有图像，则添加图像
        if (content.type === "IMAGE") {
            const img = document.createElement('img');
            img.src = 'data:image/' + content.image.format + ';base64,' + content.image.source.bytesStr;
            img.style.height = '150px'
            img.style.display = 'block';
            contentDiv.appendChild(img);
        }
    });
}
// 渲染聊天记录
function renderChatDetailHistory(chatRecords) {
    chatArea.innerHTML = '';  // 清空当前聊天记录

    chatRecords.conversations.forEach(record => {
        renderChatRecordItem(record);
    });
    chatArea.scrollTop = chatArea.scrollHeight; // 滚动到底部
}

function generateChatRecord(role, userInputValue, imgFiles) {
    const record = {};
    record.role = role;
    record.contents = [{'type': 'TEXT', 'text': userInputValue}];

    if(imgFiles.length > 0) {
        var reader = new FileReader(); // 创建FileReader对象
        reader.onload = function (e) { // 文件读取成功完成后的处理
            var imgContent = e.target.result;
            var imageType  = 'jpg';
            var imgBase64Str = '';

            const imgTypeMatch = imgContent.match(/data:image\/([a-z]{0,4});.+/);
            if (imgTypeMatch) {
                imageType = imgTypeMatch[1]; // 提取的内容在 match[1] 中
            }

            const imgBase64StrMatch = imgContent.match(/data:image\/[a-z]{0,4};base64,(.+)/);
            if (imgBase64StrMatch) {
                imgBase64Str = imgBase64StrMatch[1]; // 提取的内容在 match[1] 中
            }

            record.contents.push({'type': 'IMAGE', 'image': {"format": imageType, "source": {"bytesStr": imgBase64Str}}});
            renderChatRecordItem(record);
            renderResponseWaiting();
            chatArea.scrollTop = chatArea.scrollHeight; // 滚动到底部
        };
        reader.readAsDataURL(imgFiles[0]); // 读取文件为base64
    } else {
        renderChatRecordItem(record);
        renderResponseWaiting();
        chatArea.scrollTop = chatArea.scrollHeight; // 滚动到底部
    }
}

// 发送消息
async function sendMessage() {
    if(userInput.value.trim() === '') {
        return;
    }

    const formData = new FormData();
    formData.append('id', chatId);
    formData.append('userInput', userInput.value);
    formData.append('imgFile', imageInput.files[0]);

    generateChatRecord('user', userInput.value, imageInput.files);

    try {
        userInput.value = ''; // 清空输入框
        imageInput.value = ''; // 清空文件选择
        imageShow.src = ''; //清空图片预览
        imageShow.style.display = 'none';

        const response = await fetch('/chat', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            const jsonData = await response.json();
            removeResponseWaiting();
            renderChatRecordItem(jsonData); // 重新获取聊天记录
            chatArea.scrollTop = chatArea.scrollHeight; // 滚动到底部
        } else {
            console.error('错误:', response.statusText);
        }
    } catch (error) {
        console.error('发送消息时出错:', error);
    }
}

imageInput.addEventListener('change', function(event) {
    var file = event.target.files[0];
    var reader = new FileReader();
    reader.onload = function(e) {
        imageShow.src = e.target.result;
        imageShow.style.display = 'inline-block';
    };
    reader.readAsDataURL(file);
});

sendButton.addEventListener('click', sendMessage);
// 监听键盘按下事件
document.addEventListener('keydown',
    function(event) {
        // 检查按下的键是否是回车键
        if (event.key === 'Enter') {
            // 触发按钮的点击事件
            sendButton.click();
        }
});

async function onloadInit() {
    fetchChatRecordsSummary();
    fetchChatDetailHistory();
}

window.onload = onloadInit; // 页面加载时获取聊天记录