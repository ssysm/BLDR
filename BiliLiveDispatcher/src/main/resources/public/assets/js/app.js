document.addEventListener('DOMContentLoaded', async () => {
    var selectElems = document.querySelectorAll('select');
    M.FormSelect.init(selectElems, {});
    await loadStatus();
});

// Status Element
const poolStatusElm = document.querySelector("#poolStatus");
const nodeStatusElm = document.querySelector("#nodeStatus");
// Status Number Element
const workingNodeElm = document.querySelector("#workingNode");
const errorNodeElm = document.querySelector("#errorNode");
const vacantNodeElm = document.querySelector("#vacantNode");
const nodeTotalElm = document.querySelector("#nodeTotal");
const workerTotalElm = document.querySelector("#workerTotal");
// Add Work Element
const nodeSelectElm = document.querySelector("#nodeSelect");
const idTypeSelectElm = document.querySelector("#idTypeSelect");
const idInputElm = document.querySelector("#idInput");
const workSubmitElm = document.querySelector("#workSubmit");
// Add Node Element
const nodeInputElm = document.querySelector("#nodeInput");
const nodeSubmitElm = document.querySelector("#nodeSubmit");

// Init all variables
let workingNode = 0;
let errorNode = 0;
let vacantNode = 0;
let nodeTotal = 0;
let workerTotal = 0;

const loadStatus = async ()=>{
    // Reset to zero
    workingNode = 0;
    errorNode = 0;
    vacantNode = 0;
    nodeTotal = 0;
    workerTotal = 0;
    nodeSelectElm.innerHTML = null;
    // get pool
    const poolStatus = await getPoolStatus();
    let statusHTML = "";
    let nodeStatusHTML = "";
    for(let node of poolStatus){
        if(node.pool_size > node.active_count){
            nodeSelectElm.innerHTML += `<option value="${node.name}">${node.name} (${node.active_count}/${node.pool_size})</option>`
        }
        M.FormSelect.init(nodeSelectElm, {});
        // render Node
        statusHTML += renderNode(node);
        nodeStatusHTML += renderNodeStat(node);
    }
    loadNodeStat();
    // render to dom
    nodeTotalElm.innerHTML = poolStatus.length;
    poolStatusElm.innerHTML = statusHTML;
    nodeStatusElm.innerHTML = nodeStatusHTML;
}

const loadNodeStat = ()=>{
    workingNodeElm.innerHTML = workingNode
    errorNodeElm.innerHTML = errorNode
    vacantNodeElm.innerHTML = vacantNode
    nodeTotalElm.innerHTML = nodeTotal
    workerTotalElm.innerHTML = workerTotal
}

/** Render Methods */
// Node Job Render
const renderNode = (status) => {
    const poolSize = status.pool_size;
    const nodeName = status.name;
    workerTotal += poolSize;
    let nodeHTML = "";
    for(let [index,stat] of status.job_stat.entries()){
        let threadID = stat.thread_id,
            roomID = stat.room_id;
        let isStopped = stat.thread_stopped || (stat.status === 'E');
        let buttonOptions = "";
        switch(stat.status) {
            case 'V':
                statusType = 'vacant';
                status = '可用';
                vacantNode++;
                buttonOptions = `<td>
                <a class="btn red" onClick="deleteThread('${nodeName}','${threadID}')">删除</a>
                <a class="btn green" onClick="restartThread('${nodeName}','${threadID}')">重启</a>
                </td>`;
                break;
            case 'S':
                statusType = 'waiting';
                status = '正在等待';
                buttonOptions = `<td>
                    <a class="btn red" onClick="stopThread('${nodeName}','${threadID}')">停止</a>
                    </td>`;
                workingNode++;
                break;
            case 'E':
                statusType = 'error';
                status = '出错';
                errorNode++;
                break;
            case 'R':
                statusType = 'download';
                status = '运行中';
                buttonOptions = `<td><a class="btn red" onClick="stopThread('${nodeName}','${threadID}')">停止</a></td>`;
                workingNode++;
                break;
            case 'W':
                statusType = 'warning';
                status = '警告';
                buttonOptions = `<td><a class="btn red" onClick="stopThread('${nodeName}','${threadID}')">停止</a></td>`;
                workingNode++;
                break;
        }

        if(isStopped){
            statusType = 'stop';
            status = '停止';
            buttonOptions = `<td>
                <a class="btn green" onClick="restartThread('${nodeName}','${threadID}')">重启</a>
                <a class="btn red" onClick="deleteThread('${nodeName}','${threadID}')">删除</a>
            </td>`;
        }


        nodeHTML = `
            <tr>
                <td>${nodeName}(${index + 1}/${poolSize})</td>
                <td>${threadID}</td>
                <td>${isStopped ? "已停机" : ("录制: 房间 " + roomID)}</td>
                <td><i class="fi-${statusType} ${statusType}-color"></i>${status}</td>
                ${buttonOptions}
            </tr>`;
    }

    return nodeHTML;
}

// Node Stat Render
const renderNodeStat = (stat)=>{
    let nodeStatHTML = "";
    nodeStatHTML += `
        <tr>
            <td>${stat.name}</td>
            <td>${stat.active_count}/${stat.pool_size}</td>
            <td>上一次更新:${moment(new Date(parseInt(stat.last_update + '000'))).format('lll')}
            (${moment(new Date(parseInt(stat.last_update + '000'))).fromNow()})</td>
            <td>
                <a class="btn red" onClick="deleteNode('${stat.name}')">删除线程</a>
                <a class="btn red" onClick="shutdownPool('${stat.name}')">删除所有工作</a>
            </td>
        </td>`;
    return nodeStatHTML;
}

/** Event Handler */
// Add new job
const addNewJobHandler = (event) =>{
    let node = nodeSelectElm.options[nodeSelectElm.selectedIndex].value;
    let idType = idTypeSelectElm.options[idTypeSelectElm.selectedIndex].value;
    let id = idInputElm.value;
    if(node !== '' && idType !== '' && id !== ''){
        createThread(node,idType,id);
    }else{
        M.toast({html: '数据不完整！'});
    }
}
// Add New Node
const addNewNodeHandler = (event) =>{
    let node = nodeInputElm.value;
    if(node !== ''){
        addNewNode(node);
    }else{
        M.toast({html: '数据不完整！'});
    }
}

/** Auto Refresh */
// Refresh handler
const autoRefresh = async (event)=>{
    await loadStatus();
}
// Refresh Interval
const refreshInterval = setInterval(async (e)=>{
    await autoRefresh(e);
},CONFIG.refreshInterval);
// Set Event Handler
workSubmitElm.addEventListener('click', addNewJobHandler);
nodeSubmitElm.addEventListener('click', addNewNodeHandler);