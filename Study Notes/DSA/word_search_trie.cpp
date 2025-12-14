#include <bits/stdc++.h>
using namespace std;

class Trie
{
public:
    Trie *arr[26];
    bool eow;

    Trie()
    {
        for (int i = 0; i < 26; i++)
            arr[i] = nullptr;
        eow = false;
    }
};

class WordDictionary
{
public:
    Trie *head;

    WordDictionary()
    {
        head = new Trie();
    }

    void addWord(string word)
    {
        Trie *prev = head;
        for (char c : word)
        {
            if (!prev->arr[(int)c - 97])
            {
                prev->arr[(int)c - 97] = new Trie();
            }
            prev = prev->arr[(int)c - 97];
        }
        prev->eow = true;
    }

    bool searchHelper(string s, int i, Trie *node)
    {
        if (!node)
            return false;
        if (i == s.size())
            return node->eow;
        int x = (int)s[i] - 97;
        if (s[i] != '.')
        {
            return searchHelper(s, i + 1, node->arr[x]);
        }
        for (int j = 0; j < 26; j++)
        {
            if (searchHelper(s, i + 1, node->arr[j]))
                return true;
        }
        return false;
    }

    bool search(string s)
    {
        return searchHelper(s, 0, head);
    }

    void traverseHelper(Trie *node)
    {
        if (node->eow)
        {
            cout << " ";
            return;
        }
        for (int i = 0; i < 26; i++)
        {
            if (node->arr[i])
            {
                cout << (char)(i + 97);
                traverseHelper(node->arr[i]);
            }
        }
    }

    void traverse()
    {
        traverseHelper(head);
    }
};

int main()
{
    WordDictionary *obj = new WordDictionary();
    obj->addWord("a");
    obj->addWord("ab");
    cout << obj->search("a");
    return 0;
}