const http = require('http');
const url = require('url');

const port = 8080;

const mockData = {
  token: 'mock-jwt-token-for-demo',
  user: {
    id: 1,
    username: 'demo',
    email: 'demo@example.com',
    avatar: null
  },
  notes: [
    { 
      id: 1, 
      title: '欢迎使用记事本', 
      content: '# 欢迎\n\n这是一个演示笔记，您可以在这里编辑 Markdown 内容。', 
      summary: '欢迎使用记事本', 
      categoryId: 1, 
      tags: [], 
      createdAt: '2026-05-16 10:00:00', 
      updatedAt: '2026-05-16 10:00:00',
      isArchived: false,
      isDeleted: false
    },
    { 
      id: 2, 
      title: '我的第一个笔记', 
      content: '# 学习笔记\n\n这是我的第一个学习笔记。', 
      summary: '学习笔记摘要', 
      categoryId: 1, 
      tags: [{ id: 1, name: '重要', color: '#ff4d4f' }], 
      createdAt: '2026-05-16 11:00:00', 
      updatedAt: '2026-05-16 11:00:00',
      isArchived: false,
      isDeleted: false
    }
  ],
  categories: [
    { id: 1, name: '默认分类', sortOrder: 0 }
  ],
  tags: [
    { id: 1, name: '重要', color: '#ff4d4f' },
    { id: 2, name: '工作', color: '#1890ff' }
  ]
};

const handleRequest = (req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;
  const method = req.method;

  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (method === 'OPTIONS') {
    res.writeHead(200);
    res.end();
    return;
  }

  console.log(`${method} ${pathname}`);

  // 获取请求体
  const getBody = () => {
    return new Promise((resolve, reject) => {
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', () => {
        try {
          resolve(body ? JSON.parse(body) : {});
        } catch (e) {
          reject(e);
        }
      });
    });
  };

  // 认证相关
  if (pathname === '/api/auth/login' && method === 'POST') {
    getBody().then(data => {
      if (data.username && data.password) {
        res.writeHead(200);
        res.end(JSON.stringify({
          code: 200,
          message: '登录成功',
          data: {
            token: mockData.token,
            user: mockData.user
          }
        }));
      } else {
        res.writeHead(401);
        res.end(JSON.stringify({ code: 401, message: '用户名或密码错误' }));
      }
    });
  } else if (pathname === '/api/auth/register' && method === 'POST') {
    getBody().then(data => {
      res.writeHead(200);
      res.end(JSON.stringify({
        code: 200,
        message: '注册成功',
        data: { id: 1, username: data.username, email: data.email }
      }));
    });
  } else if (pathname === '/api/auth/userinfo' && method === 'GET') {
    res.writeHead(200);
    res.end(JSON.stringify({
      code: 200,
      data: mockData.user
    }));
  } 
  // 笔记相关
  else if (pathname === '/api/notes' && method === 'GET') {
    res.writeHead(200);
    res.end(JSON.stringify({
      code: 200,
      data: {
        records: mockData.notes,
        total: mockData.notes.length,
        page: 1,
        size: 20
      }
    }));
  } else if (pathname === '/api/notes' && method === 'POST') {
    getBody().then(data => {
      const newNote = {
        id: Date.now(),
        title: data.title,
        content: data.content,
        summary: data.content?.substring(0, 200),
        categoryId: data.categoryId,
        tags: data.tagIds?.map(id => mockData.tags.find(t => t.id === id)) || [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        isArchived: false,
        isDeleted: false
      };
      mockData.notes.unshift(newNote);
      res.writeHead(200);
      res.end(JSON.stringify({
        code: 200,
        message: '创建成功',
        data: newNote
      }));
    });
  } else if (pathname.match(/^\/api\/notes\/\d+$/) && method === 'GET') {
    const id = parseInt(pathname.split('/')[3]);
    const note = mockData.notes.find(n => n.id === id);
    if (note) {
      res.writeHead(200);
      res.end(JSON.stringify({ code: 200, data: note }));
    } else {
      res.writeHead(404);
      res.end(JSON.stringify({ code: 404, message: '笔记不存在' }));
    }
  } else if (pathname.match(/^\/api\/notes\/\d+$/) && method === 'PUT') {
    const id = parseInt(pathname.split('/')[3]);
    getBody().then(data => {
      const noteIndex = mockData.notes.findIndex(n => n.id === id);
      if (noteIndex !== -1) {
        mockData.notes[noteIndex] = {
          ...mockData.notes[noteIndex],
          ...data,
          updatedAt: new Date().toISOString()
        };
        res.writeHead(200);
        res.end(JSON.stringify({ code: 200, data: mockData.notes[noteIndex] }));
      } else {
        res.writeHead(404);
        res.end(JSON.stringify({ code: 404, message: '笔记不存在' }));
      }
    });
  } else if (pathname.match(/^\/api\/notes\/\d+$/) && method === 'DELETE') {
    const id = parseInt(pathname.split('/')[3]);
    const noteIndex = mockData.notes.findIndex(n => n.id === id);
    if (noteIndex !== -1) {
      mockData.notes.splice(noteIndex, 1);
      res.writeHead(200);
      res.end(JSON.stringify({ code: 200, message: '删除成功' }));
    } else {
      res.writeHead(404);
      res.end(JSON.stringify({ code: 404, message: '笔记不存在' }));
    }
  } else if (pathname.match(/^\/api\/notes\/\d+\/archive$/) && method === 'PUT') {
    const id = parseInt(pathname.split('/')[3]);
    getBody().then(data => {
      const noteIndex = mockData.notes.findIndex(n => n.id === id);
      if (noteIndex !== -1) {
        mockData.notes[noteIndex].isArchived = data.isArchived;
        res.writeHead(200);
        res.end(JSON.stringify({ code: 200, data: mockData.notes[noteIndex] }));
      } else {
        res.writeHead(404);
        res.end(JSON.stringify({ code: 404, message: '笔记不存在' }));
      }
    });
  }
  // 分类相关
  else if (pathname === '/api/categories' && method === 'GET') {
    res.writeHead(200);
    res.end(JSON.stringify({
      code: 200,
      data: mockData.categories
    }));
  }
  // 标签相关
  else if (pathname === '/api/tags' && method === 'GET') {
    res.writeHead(200);
    res.end(JSON.stringify({
      code: 200,
      data: mockData.tags
    }));
  }
  // 404
  else {
    res.writeHead(404);
    res.end(JSON.stringify({ code: 404, message: '接口不存在' }));
  }
};

const server = http.createServer(handleRequest);

server.listen(port, '0.0.0.0', () => {
  console.log(`Mock API Server running at http://0.0.0.0:${port}/`);
  console.log('Available endpoints:');
  console.log('  POST /api/auth/login');
  console.log('  POST /api/auth/register');
  console.log('  GET  /api/auth/userinfo');
  console.log('  GET  /api/notes');
  console.log('  POST /api/notes');
  console.log('  GET  /api/notes/:id');
  console.log('  PUT  /api/notes/:id');
  console.log('  DELETE /api/notes/:id');
  console.log('  PUT  /api/notes/:id/archive');
  console.log('  GET  /api/categories');
  console.log('  GET  /api/tags');
});
