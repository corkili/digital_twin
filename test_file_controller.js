/**
 * FileController 接口测试
 * 测试文件上传下载预签名URL接口
 */

const axios = require('axios');
const fs = require('fs');
const path = require('path');

// 配置基础URL，根据你的服务器地址修改
const BASE_URL = 'http://umi.xyz:8081/api';
const API_BASE = `${BASE_URL}/simulations/files`;

// 测试用例配置
const testConfig = {
    upload: {
        fileName: 'test-upload-file.txt',
        operationType: 'UPLOAD',
        expiry: 30, // 30分钟
        contentType: 'text/plain'
    },
    download: {
        fileName: 'test-download-file.txt',
        operationType: 'DOWNLOAD',
        expiry: 60, // 60分钟
        contentType: 'text/plain'
    }
};

/**
 * 测试生成上传预签名URL
 */
async function testGenerateUploadPresignedUrl() {
    console.log('\n=== 测试生成上传预签名URL ===');

    try {
        const response = await axios.post(`${API_BASE}/presigned-url`, testConfig.upload, {
            headers: {
                'Content-Type': 'application/json'
            }
        });

        console.log('✅ 上传预签名URL生成成功');
        console.log('状态码:', response.status);
        console.log('响应数据:', JSON.stringify(response.data, null, 2));

        // 验证响应结构
        const data = response.data.data;
        if (data.presignedUrl && data.fileName && data.operationType === 'UPLOAD') {
            console.log('✅ 响应数据结构正确');
            return data.presignedUrl;
        } else {
            console.log('❌ 响应数据结构不完整');
        }

    } catch (error) {
        console.log('❌ 上传预签名URL生成失败');
        console.log('错误信息:', error.response?.data || error.message);
    }
}

/**
 * 测试生成下载预签名URL
 */
async function testGenerateDownloadPresignedUrl() {
    console.log('\n=== 测试生成下载预签名URL ===');

    try {
        const response = await axios.post(`${API_BASE}/presigned-url`, testConfig.download, {
            headers: {
                'Content-Type': 'application/json'
            }
        });

        console.log('✅ 下载预签名URL生成成功');
        console.log('状态码:', response.status);
        console.log('响应数据:', JSON.stringify(response.data, null, 2));

        // 验证响应结构
        const data = response.data.data;
        if (data.presignedUrl && data.fileName && data.operationType === 'DOWNLOAD') {
            console.log('✅ 响应数据结构正确');
            return data.presignedUrl;
        } else {
            console.log('❌ 响应数据结构不完整');
        }

    } catch (error) {
        console.log('❌ 下载预签名URL生成失败');
        console.log('错误信息:', error.response?.data || error.message);
    }
}

/**
 * 测试获取MinIO服务器信息
 */
async function testGetServerInfo() {
    console.log('\n=== 测试获取MinIO服务器信息 ===');

    try {
        const response = await axios.get(`${API_BASE}/server-info`);

        console.log('✅ 获取服务器信息成功');
        console.log('状态码:', response.status);
        console.log('响应数据:', JSON.stringify(response.data, null, 2));

        // 验证响应结构
        const data = response.data.data;
        if (data.endpoint && data.bucketName) {
            console.log('✅ 服务器信息结构正确');
        } else {
            console.log('❌ 服务器信息结构不完整');
        }

    } catch (error) {
        console.log('❌ 获取服务器信息失败');
        console.log('错误信息:', error.response?.data || error.message);
    }
}

/**
 * 创建测试文件
 */
function createTestFile() {
    const testFileName = 'test-upload-file.txt';
    const testContent = `测试文件内容
创建时间: ${new Date().toISOString()}
这是一个真实的测试文件用于验证MinIO上传功能
文件大小测试数据: ${'x'.repeat(1000)}`;

    fs.writeFileSync(testFileName, testContent);
    console.log(`✅ 创建测试文件: ${testFileName} (${testContent.length} bytes)`);
    return testFileName;
}

/**
 * 测试实际文件上传（使用预签名URL）
 */
async function testActualFileUpload(presignedUrl, fileName) {
    if (!presignedUrl) {
        console.log('\n⚠️  跳过实际文件上传测试（未获取到预签名URL）');
        return;
    }

    console.log('\n=== 测试实际文件上传 ===');

    try {
        // 读取真实文件
        const filePath = path.resolve(fileName);
        if (!fs.existsSync(filePath)) {
            console.log('❌ 测试文件不存在:', filePath);
            return;
        }

        const fileBuffer = fs.readFileSync(filePath);
        const fileSize = fs.statSync(filePath).size;

        console.log(`📁 上传文件: ${fileName}`);
        console.log(`📏 文件大小: ${fileSize} bytes`);

        const response = await axios.put(presignedUrl, fileBuffer, {
            headers: {
                'Content-Type': 'text/plain',
                'Content-Length': fileSize
            },
            maxBodyLength: Infinity,
            maxContentLength: Infinity
        });

        console.log('✅ 文件上传成功');
        console.log('状态码:', response.status);
        console.log('响应头:', response.headers);

    } catch (error) {
        console.log('❌ 文件上传失败');
        console.log('错误信息:', error.response?.data || error.message);
        if (error.response) {
            console.log('响应状态:', error.response.status);
            console.log('响应头:', error.response.headers);
        }
    }
}

/**
 * 测试实际文件下载（使用预签名URL）
 */
async function testActualFileDownload(fileName) {
    console.log('\n=== 测试实际文件下载 ===');

    try {
        // 首先生成下载预签名URL
        const urlResponse = await axios.post(`${API_BASE}/presigned-url`, {
            fileName: fileName,
            operationType: 'DOWNLOAD',
            expiry: 30,
            contentType: 'text/plain'
        });

        if (!urlResponse.data.success) {
            console.log('❌ 生成下载预签名URL失败');
            return;
        }

        const presignedUrl = urlResponse.data.data.presignedUrl;
        console.log('✅ 下载预签名URL生成成功');

        // 使用预签名URL下载文件
        const downloadResponse = await axios.get(presignedUrl, {
            responseType: 'arraybuffer'
        });

        console.log('✅ 文件下载成功');
        console.log('状态码:', downloadResponse.status);
        console.log('文件大小:', downloadResponse.data.byteLength, 'bytes');
        console.log('Content-Type:', downloadResponse.headers['content-type']);

        // 保存下载的文件
        const downloadedFileName = `downloaded_${fileName}`;
        fs.writeFileSync(downloadedFileName, downloadResponse.data);
        console.log(`💾 文件已保存为: ${downloadedFileName}`);

        // 验证下载的文件内容（如果是文本文件）
        if (fileName.endsWith('.txt')) {
            const downloadedContent = fs.readFileSync(downloadedFileName, 'utf8');
            console.log('📄 下载文件内容预览:');
            console.log(downloadedContent.substring(0, 200) + (downloadedContent.length > 200 ? '...' : ''));
        }

        return downloadedFileName;

    } catch (error) {
        console.log('❌ 文件下载失败');
        console.log('错误信息:', error.response?.data || error.message);
        if (error.response) {
            console.log('响应状态:', error.response.status);
        }
    }
}

/**
 * 测试文件上传下载完整流程
 */
async function testCompleteUploadDownloadFlow() {
    console.log('\n=== 测试完整上传下载流程 ===');

    const testFileName = 'flow-test-file.txt';
    const testContent = `完整流程测试文件
创建时间: ${new Date().toISOString()}
测试内容: 这是用于验证上传下载完整流程的文件
随机数据: ${Math.random().toString(36).substring(2, 15)}`;

    try {
        // 1. 创建测试文件
        fs.writeFileSync(testFileName, testContent);
        console.log(`📁 创建测试文件: ${testFileName}`);

        // 2. 生成上传预签名URL
        const uploadUrlResponse = await axios.post(`${API_BASE}/presigned-url`, {
            fileName: testFileName,
            operationType: 'UPLOAD',
            expiry: 30,
            contentType: 'text/plain'
        });

        if (!uploadUrlResponse.data.success) {
            console.log('❌ 生成上传预签名URL失败');
            return;
        }

        const uploadUrl = uploadUrlResponse.data.data.presignedUrl;
        console.log('✅ 上传预签名URL生成成功');

        // 3. 上传文件
        const fileBuffer = fs.readFileSync(testFileName);
        await axios.put(uploadUrl, fileBuffer, {
            headers: {
                'Content-Type': 'text/plain',
                'Content-Length': fileBuffer.length
            }
        });
        console.log('✅ 文件上传成功');

        // 4. 等待一下确保文件上传完成
        await new Promise(resolve => setTimeout(resolve, 1000));

        // 5. 下载文件
        const downloadedFileName = await testActualFileDownload(testFileName);

        // 6. 比较原始文件和下载文件
        if (downloadedFileName) {
            const originalContent = fs.readFileSync(testFileName, 'utf8');
            const downloadedContent = fs.readFileSync(downloadedFileName, 'utf8');

            if (originalContent === downloadedContent) {
                console.log('✅ 文件内容验证通过 - 上传下载完整性正确');
            } else {
                console.log('❌ 文件内容不一致');
                console.log('原始长度:', originalContent.length);
                console.log('下载长度:', downloadedContent.length);
            }

            // 清理下载的文件
            fs.unlinkSync(downloadedFileName);
        }

        // 清理测试文件
        fs.unlinkSync(testFileName);

    } catch (error) {
        console.log('❌ 完整流程测试失败:', error.message);

        // 清理可能存在的文件
        [testFileName, `downloaded_${testFileName}`].forEach(file => {
            if (fs.existsSync(file)) {
                fs.unlinkSync(file);
            }
        });
    }
}

/**
 * 测试不同类型文件上传
 */
async function testDifferentFileTypes() {
    console.log('\n=== 测试不同文件类型上传 ===');

    const testFiles = [
        {
            name: 'test-image.png',
            content: Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==', 'base64'),
            contentType: 'image/png'
        },
        {
            name: 'test-json.json',
            content: JSON.stringify({test: 'data', timestamp: new Date().toISOString()}, null, 2),
            contentType: 'application/json'
        },
        {
            name: 'test-binary.bin',
            content: Buffer.from([0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64]),
            contentType: 'application/octet-stream'
        }
    ];

    for (const file of testFiles) {
        try {
            // 创建文件
            fs.writeFileSync(file.name, file.content);
            console.log(`📁 创建测试文件: ${file.name} (${file.content.length} bytes)`);

            // 获取预签名URL
            const urlResponse = await axios.post(`${API_BASE}/presigned-url`, {
                fileName: file.name,
                operationType: 'UPLOAD',
                expiry: 30,
                contentType: file.contentType
            });

            if (urlResponse.data.success) {
                const presignedUrl = urlResponse.data.data.presignedUrl;

                // 上传文件
                const uploadResponse = await axios.put(presignedUrl, file.content, {
                    headers: {
                        'Content-Type': file.contentType,
                        'Content-Length': file.content.length
                    }
                });

                console.log(`✅ ${file.name} 上传成功 (状态: ${uploadResponse.status})`);
            }

        } catch (error) {
            console.log(`❌ ${file.name} 上传失败:`, error.message);
        }
    }
}

/**
 * 测试参数验证
 */
async function testParameterValidation() {
    console.log('\n=== 测试参数验证 ===');

    // 测试缺少必要参数
    const invalidRequests = [
        {
            description: '缺少fileName',
            data: {
                operationType: 'UPLOAD',
                expiry: 30
            }
        },
        {
            description: '缺少operationType',
            data: {
                fileName: 'test.txt',
                expiry: 30
            }
        },
        {
            description: '无效的operationType',
            data: {
                fileName: 'test.txt',
                operationType: 'INVALID',
                expiry: 30
            }
        }
    ];

    for (const testCase of invalidRequests) {
        try {
            await axios.post(`${API_BASE}/presigned-url`, testCase.data);
            console.log(`❌ ${testCase.description} - 应该返回错误但请求成功了`);
        } catch (error) {
            console.log(`✅ ${testCase.description} - 正确返回错误:`, error.response?.status);
        }
    }
}

/**
 * 清理测试文件
 */
function cleanupTestFiles() {
    const testFiles = [
        'test-upload-file.txt',
        'test-image.png',
        'test-json.json',
        'test-binary.bin',
        'flow-test-file.txt',
        'downloaded_test-upload-file.txt',
        'downloaded_flow-test-file.txt'
    ];

    console.log('\n🧹 清理测试文件...');
    testFiles.forEach(file => {
        if (fs.existsSync(file)) {
            fs.unlinkSync(file);
            console.log(`🗑️  删除: ${file}`);
        }
    });
}

/**
 * 主测试函数
 */
async function runAllTests() {
    console.log('🚀 开始测试 FileController 接口...');
    console.log('服务器地址:', BASE_URL);

    try {
        // 创建测试文件
        const testFileName = createTestFile();

        // 测试获取服务器信息
        await testGetServerInfo();

        // 测试生成预签名URL
        const uploadUrl = await testGenerateUploadPresignedUrl();
        await testGenerateDownloadPresignedUrl();

        // 测试实际文件上传
        await testActualFileUpload(uploadUrl, testFileName);

        // 测试不同文件类型上传
        await testDifferentFileTypes();

        // 测试完整上传下载流程
        await testCompleteUploadDownloadFlow();

        // 测试参数验证
        await testParameterValidation();

    } catch (error) {
        console.error('❌ 测试过程中出现错误:', error.message);
    } finally {
        // 清理测试文件
        cleanupTestFiles();
    }

    console.log('\n🎉 测试完成！');
    console.log('\n📝 使用说明:');
    console.log('1. 确保你的Spring Boot服务正在运行');
    console.log('2. 修改 BASE_URL 为你的实际服务地址');
    console.log('3. 确保MinIO服务正常运行');
    console.log('4. 运行: node test_file_controller.js');
}

// 运行测试
if (require.main === module) {
    runAllTests().catch(console.error);
}

module.exports = {
    testGenerateUploadPresignedUrl,
    testGenerateDownloadPresignedUrl,
    testGetServerInfo,
    testActualFileUpload,
    testActualFileDownload,
    testCompleteUploadDownloadFlow,
    testDifferentFileTypes,
    testParameterValidation,
    createTestFile,
    cleanupTestFiles
};