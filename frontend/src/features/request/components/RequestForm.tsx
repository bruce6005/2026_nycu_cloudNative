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
        <div className="card mb-2">
            <h3 className="mb-1">建立新委託</h3>
            <form onSubmit={handleSubmit} className="column">
                <div className="form-group">
                    <label className="label">委託單標題</label>
                    <input
                        placeholder="請輸入標題..."
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                    />
                </div>

                <div className="form-group">
                    <label className="label">優先度 (1最高 5最低)</label>
                    <input
                        type="number"
                        placeholder="預設為 5"
                        value={priority}
                        onChange={(e) => setPriority(e.target.value === '' ? '' : Number(e.target.value))}
                    />
                </div>

                <div className="form-group">
                    <label className="label">詳細描述</label>
                    <textarea
                        placeholder="請輸入委託內容描述..."
                        rows={3}
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    />
                </div>

                <button type="submit" className="button primary mt-1">
                    建立委託單
                </button>
            </form>
        </div>
    );
};
