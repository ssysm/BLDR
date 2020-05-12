const stopThread = (node,threadID) =>{
    axios.patch('/work/',{}, {
        params: {
            node,
            thread_id: threadID
        }
    })
    .then(res => res.data)
    .then(data => {
        M.toast({html: 'Request Sent!<br>Requesting to stop Thread: ' + data.thread_id});
    })
    .catch(e => {
        M.toast({html: 'Error while requesting... '});
    })
}

const deleteThread = (node,threadID) =>{
    axios.delete('/work/', {
        params: {
            node,
            thread_id: threadID
        }
    })
    .then(res => res.data)
    .then(data => {
        M.toast({html: 'Request Sent!<br>Requesting to delete Thread: ' + data.thread_id});
    })
    .catch(e => {
        M.toast({html: 'Error while requesting... '});

    })
}

const createThread = (node, idType, id) =>{
    axios.post('/work/',{}, {
        params: {
            node,
            id_type: idType,
            id
        }
    })
    .then(res => res.data)
    .then(data => {
        M.toast({html: 'Request Sent!<br>Requesting to create thread to watch ID: ' + data.id});
    })
    .catch(e => {
        M.toast({html: 'Error while requesting... '});
    })
}

const restartThread = (node,threadID) =>{
    axios.put('/work/',{}, {
        params: {
            node,
            thread_id: threadID
        }
    })
    .then(res => res.data)
    .then(data => {
        M.toast({html: 'Request Sent!<br>Requesting to restart Thread: ' + data.thread_id});
    })
    .catch(e => {
        M.toast({html: 'Error while requesting... '});
    })
}

const getPoolStatus = async () => {
    return await axios.get('/pool/status')
        .then(res => res.data)
        .catch(e=>{
            M.toast({html:"无法连接到发布平台！"});
        })
}


const shutdownPool = (node) => {
    axios.delete('/pool/shutdown',{
        params: {
            node
        }
    })
        .then(res => res.data)
        .then(data => {
            M.toast({html: 'Request Sent!<br>Requesting to shutdown node: ' + node});
        })
        .catch(e => {
            M.toast({html: 'Error while requesting... '});
        })
}

const addNewNode = (node) => {
    axios.post('/node/',{},{
        params: {
            node
        }
    })
        .then(res => res.data)
        .then(data => {
            if(data == -1){
                M.toast({html: '无法找到节点，请确保在redis池内。'});
            }else if(data == 0) {
                M.toast({html: '节点已存在'});
            }else if(data == 1){
                M.toast({html: '节点添加请求已送达,添加:' + node});
            }
        })
        .catch(e => {
            M.toast({html: 'Error while requesting... '});
        })
}

const deleteNode = (node) => {
    axios.delete('/node/',{
        params: {
            node
        }
    })
        .then(res => res.data)
        .then(data => {
            M.toast({html: 'Request Sent!<br>Requesting to delete node: ' + node});
        })
        .catch(e => {
            M.toast({html: 'Error while requesting... '});
        })
}