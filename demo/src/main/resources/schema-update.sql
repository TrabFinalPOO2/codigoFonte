-- Verifica se a coluna já existe antes de tentar adicioná-la
ALTER TABLE COMMENT ADD COLUMN IF NOT EXISTS LIKES_COUNT INT DEFAULT 0;

-- Atualiza os valores existentes para sincronizar o contador com o número real de likes
-- Isso será útil para comentários que já existiam antes da adição da coluna
UPDATE COMMENT C
SET LIKES_COUNT = (
    SELECT COUNT(*) 
    FROM COMMENT_LIKED_USERS CLU 
    WHERE CLU.COMMENT_ID = C.ID
);