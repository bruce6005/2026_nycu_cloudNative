import React, { useState } from 'react';
import { createRequest } from '../api/requestApi';

interface RequestFormProps {
    userId: number;
    onSuccess: () => void; // 成功後重整清單
}

export const RequestForm: React.FC<RequestFormProps> = ({ userId,  onSuccess }) => {
    // 狀態記憶體
    const [title, setTitle] = useState('');
    const [priority, setPriority] = useState<number | ''>(''); // 初始為空讓 placeholder 顯示
    const [description, setDescription] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await createRequest({
                title,
                factoryUserId: userId,
                priority: priority === '' ? 5 : Number(priority), // 若沒填則預設 5
                description
            } as any);
            alert('建立成功！');
            // 清空表單內容
            setTitle('');
            setPriority('');
            setDescription('');
            onSuccess(); // 讓外層頁面更新清單
        } catch (error) {
            alert('建立失敗，請確認後台是否開啟');
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            {/* 第一排：標題 */}
            <div className="form-row" style={{ marginBottom: '10px' }}>
                <input
                    placeholder="委託單標題 (必填)"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                />
            </div>

            {/* 第二排：人員與優先度 */}
            <div className="form-row" style={{ marginBottom: '10px' }}>
                <input
                    type="number"
                    placeholder="優先度 (1最高 5最低)"
                    value={priority}
                    onChange={(e) => setPriority(e.target.value === '' ? '' : Number(e.target.value))}
                />
            </div>

            {/* 第三排：描述與按鈕 */}
            <div className="form-row">
                <textarea
                    placeholder="請輸入詳細描述..."
                    style={{ flex: 1, padding: '10px', borderRadius: '4px', border: '1px solid #d0d0d0' }}
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                />
                <button type="submit">建立委託</button>
            </div>
        </form>
    );
};
